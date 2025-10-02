package com.rsi.comelit.controller;

import com.rsi.comelit.dto.*;
import com.rsi.comelit.entity.User;
import com.rsi.comelit.repository.UserRepository;
import com.rsi.comelit.response.PostResponse;
import com.rsi.comelit.response.UserResponse;
import com.rsi.comelit.service.EmailService;
import com.rsi.comelit.service.PostService;
import com.rsi.comelit.service.UserService;
import com.rsi.comelit.util.GenerateRandomPassword;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final PostService postService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final GenerateRandomPassword generateRandomPassword;

    @GetMapping("/getAll")
    public Map<String, Object> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(required = false) String search) {

        // Create sort object
        Sort sort = Sort.by(sortBy);
        sort = "desc".equalsIgnoreCase(sortOrder) ? sort.descending() : sort.ascending();

        // Create pageable with consistent sorting
        Pageable pageable = PageRequest.of(page, limit, sort);

        Page<User> utilisateurPage;
        if (search != null && !search.trim().isEmpty()) {
            // Apply search with pagination and sorting
            utilisateurPage = userRepository.search(search.trim(), pageable);
        } else {
            // Get all users with pagination and sorting
            utilisateurPage = userRepository.findAllUsers(pageable);
        }

        // Handle case where requested page exceeds available pages
        if (page > 0 && utilisateurPage.getTotalPages() > 0 && page >= utilisateurPage.getTotalPages()) {
            // Redirect to last available page
            int lastPage = utilisateurPage.getTotalPages() - 1;
            Pageable correctedPageable = PageRequest.of(lastPage, limit, sort);
            if (search != null && !search.trim().isEmpty()) {
                utilisateurPage = userRepository.search(search.trim(), correctedPageable);
            } else {
                utilisateurPage = userRepository.findAllUsers(correctedPageable);
            }
        }

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("users", utilisateurPage.getContent());
        response.put("total", utilisateurPage.getTotalElements());
        response.put("page", utilisateurPage.getNumber());
        response.put("limit", utilisateurPage.getSize());
        response.put("totalPages", utilisateurPage.getTotalPages());
        response.put("hasNext", utilisateurPage.hasNext());
        response.put("hasPrevious", utilisateurPage.hasPrevious());

        // Debug info (remove in production)
        System.out.println("DEBUG - Requested page: " + page + ", Actual page: " + utilisateurPage.getNumber());
        System.out.println("DEBUG - Total pages: " + utilisateurPage.getTotalPages() + ", Total elements: " + utilisateurPage.getTotalElements());

        return response;
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping("/add")
    public User create(@RequestPart(value = "user", required = true) User user, @RequestPart(value = "avatar", required = false) MultipartFile avatar) throws IOException {
        user.setRole("ROLE_USER");

        // Generate random password
        String randomPassword = generateRandomPassword.generate(10);
        user.setPassword(passwordEncoder.encode(randomPassword));// store encrypted

        String uploadDir = "D:\\RSIComelit-Back-end\\src\\main\\resources\\avatars";
        if (avatar != null && !avatar.isEmpty()) {
            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) uploadFolder.mkdirs();

            String filename = System.currentTimeMillis() + "_" + avatar.getOriginalFilename();
            File file = new File(uploadFolder, filename);

            avatar.transferTo(file);
            user.setProfilePhoto("/avatars/" + filename); // for serving via controller
        }


        // Save user
        User savedUser = userRepository.save(user);

        // Send email with credentials
        String subject = "Your account has been created";
        String content = "Hello " + user.getFirstName() + ",\n\n"
                + "Your account has been successfully created.\n"
                + "Email: " + user.getEmail() + "\n"
                + "Password: " + randomPassword + "\n\n"
                + "Please change your password after first login.\n\n"
                + "Best regards,\n"
                + "The Team";

        emailService.send(user.getEmail(), subject, content);

        return ResponseEntity.ok(savedUser).getBody();
    }

    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @Valid @RequestBody User user) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update only allowed fields
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setEmail(user.getEmail());
        existingUser.setSalaire(user.getSalaire());
        existingUser.setExperience(user.getExperience());
        existingUser.setDateDebutContrat(user.getDateDebutContrat());
        existingUser.setSite(user.getSite());
        existingUser.setProcessus(user.getProcessus());
        existingUser.setCin(user.getCin());
        existingUser.setGender(user.getGender());
        existingUser.setHoursOfWork(user.getHoursOfWork());
        existingUser.setCertification(user.getCertification());
        existingUser.setYearsOfExperience(user.getYearsOfExperience());
        // Do not modify fields like id, password, role, etc.

        return userRepository.save(existingUser);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> showUserProfile(Authentication authentication) {
        User user = userService.getUserByEmail(authentication.getPrincipal().toString());
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/account/update/info")
    public ResponseEntity<?> updateUserInfo(@RequestBody @Valid UpdateUserInfoDto updateUserInfoDto) {
        User updatedUser = userService.updateUserInfo(updateUserInfoDto);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @PostMapping("/account/update/email")
    public ResponseEntity<?> updateUserEmail(@RequestBody @Valid UpdateEmailDto updateEmailDto) {
        userService.updateEmail(updateEmailDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/account/update/password")
    public ResponseEntity<?> updateUserPassword(@RequestBody @Valid UpdatePasswordDto updatePasswordDto) {
        userService.updatePassword(updatePasswordDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/account/update/profile-photo")
    public ResponseEntity<?> updateProfilePhoto(@RequestParam("profilePhoto") MultipartFile profilePhoto) {
        User updatedUser = userService.updateProfilePhoto(profilePhoto);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @PostMapping("/account/update/cover-photo")
    public ResponseEntity<?> updateCoverPhoto(@RequestParam("coverPhoto") MultipartFile coverPhoto) {
        User updatedUser = userService.updateCoverPhoto(coverPhoto);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @PostMapping("/account/delete")
    public ResponseEntity<?> deleteUserAccount() {
        userService.deleteUserAccount();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/account/follow/{userId}")
    public ResponseEntity<?> followUser(@PathVariable("userId") Long userId) {
        userService.followUser(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/account/unfollow/{userId}")
    public ResponseEntity<?> unfollowUser(@PathVariable("userId") Long userId) {
        userService.unfollowUser(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<?> getUserFollowingUsers(@PathVariable("userId") Long userId,
                                                   @RequestParam("page") Integer page,
                                                   @RequestParam("size") Integer size) {
        page = page < 0 ? 0 : page - 1;
        size = size <= 0 ? 5 : size;
        List<UserResponse> followingList = userService.getFollowingUsersPaginate(userId, page, size);
        return new ResponseEntity<>(followingList, HttpStatus.OK);
    }

    @GetMapping("/{userId}/follower")
    public ResponseEntity<?> getUserFollowerUsers(@PathVariable("userId") Long userId,
                                                  @RequestParam("page") Integer page,
                                                  @RequestParam("size") Integer size) {
        page = page < 0 ? 0 : page - 1;
        size = size <= 0 ? 5 : size;
        List<UserResponse> followingList = userService.getFollowerUsersPaginate(userId, page, size);
        return new ResponseEntity<>(followingList, HttpStatus.OK);
    }

    @GetMapping("followed-by-auth-user/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable("userId") Long userId) {
        User targetUser = userService.getUserById(userId);
        UserResponse userResponse = UserResponse.builder()
                .user(targetUser)
                .followedByAuthUser(targetUser.getFollowerUsers().contains(targetUser))
                .build();
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @GetMapping("/{userId}/posts")
    public ResponseEntity<?> getUserPosts(@PathVariable("userId") Long userId,
                                          @RequestParam("page") Integer page,
                                          @RequestParam("size") Integer size) {
        page = page < 0 ? 0 : page - 1;
        size = size <= 0 ? 5 : size;
        User targetUser = userService.getUserById(userId);
        List<PostResponse> userPosts = postService.getPostsByUserPaginate(targetUser, page, size);
        return new ResponseEntity<>(userPosts, HttpStatus.OK);
    }

    @GetMapping("/posts")
    public ResponseEntity<?> getAllPosts(@RequestParam("page") Integer page,
                                         @RequestParam("size") Integer size) {
        page = page < 0 ? 0 : page - 1;
        size = size <= 0 ? 5 : size;
        List<PostResponse> userPosts = postService.getPostsPaginate(page, size);
        return new ResponseEntity<>(userPosts, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUser(@RequestParam("key") String key,
                                        @RequestParam("page") Integer page,
                                        @RequestParam("size") Integer size) {
        page = page < 0 ? 0 : page - 1;
        size = size <= 0 ? 5 : size;
        List<UserResponse> userSearchResult = userService.getUserSearchResult(key, page, size);
        return new ResponseEntity<>(userSearchResult, HttpStatus.OK);
    }

    @GetMapping("/simple-search")
    public ResponseEntity<List<UserSimpleDto>> simpleUserSearch(@RequestParam("query") String query) {
        List<UserSimpleDto> result = userService.searchUsersByNameOrFirstname(query);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/chat")
    public ResponseEntity<List<UserChatDto>> simpleUserSearch() {
        List<UserChatDto> result = userService.getAllChatUsers();
        return ResponseEntity.ok(result);
    }
}
