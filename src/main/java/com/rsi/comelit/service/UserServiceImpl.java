package com.rsi.comelit.service;

import com.rsi.comelit.common.AppConstants;
import com.rsi.comelit.common.UserPrincipal;
import com.rsi.comelit.dto.*;
import com.rsi.comelit.entity.Comment;
import com.rsi.comelit.entity.Country;
import com.rsi.comelit.entity.Post;
import com.rsi.comelit.entity.User;
import com.rsi.comelit.enumeration.Role;
import com.rsi.comelit.exception.EmailExistsException;
import com.rsi.comelit.exception.InvalidOperationException;
import com.rsi.comelit.exception.SameEmailUpdateException;
import com.rsi.comelit.exception.UserNotFoundException;
import com.rsi.comelit.filter.JwtAuthenticationFilter;
import com.rsi.comelit.mapper.MapStructMapper;
import com.rsi.comelit.mapper.MapstructMapperUpdate;
import com.rsi.comelit.repository.UserRepository;
import com.rsi.comelit.response.UserResponse;
import com.rsi.comelit.util.FileNamingUtil;
import com.rsi.comelit.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final CountryService countryService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final MapStructMapper mapStructMapper;
    private final MapstructMapperUpdate mapstructMapperUpdate;
    private final Environment environment;
    private final FileNamingUtil fileNamingUtil;
    private final FileUploadUtil fileUploadUtil;
    @Autowired
    JwtService jwtService;
    @Autowired
    private javax.servlet.http.HttpServletRequest request;

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
    }

    @Override
    public List<UserResponse> getFollowerUsersPaginate(Long userId, Integer page, Integer size) {
        User targetUser = getUserById(userId);
        return userRepository.findUsersByFollowingUsers(targetUser, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "firstName", "lastName"))).stream().map(this::userToUserResponse).collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getFollowingUsersPaginate(Long userId, Integer page, Integer size) {
        User targetUser = getUserById(userId);
        return userRepository.findUsersByFollowerUsers(targetUser, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "firstName", "lastName"))).stream().map(this::userToUserResponse).collect(Collectors.toList());
    }

    @Override
    public User createNewUser(SignupDto signupDto) {
        try {
            User user = getUserByEmail(signupDto.getEmail());
            if (user != null) {
                throw new EmailExistsException();
            }
        } catch (UserNotFoundException e) {
            User newUser = new User();
            newUser.setEmail(signupDto.getEmail());
            newUser.setPassword(passwordEncoder.encode(signupDto.getPassword()));
            newUser.setFirstName(signupDto.getFirstName());
            newUser.setLastName(signupDto.getLastName());
            newUser.setFollowerCount(0);
            newUser.setFollowingCount(0);
            newUser.setEnabled(true);
            newUser.setAccountVerified(false);
            newUser.setEmailVerified(false);
            newUser.setJoinDate(new Date());
            newUser.setDateLastModified(new Date());
            newUser.setRole(Role.ROLE_USER.name());
            User savedUser = userRepository.save(newUser);
            UserPrincipal userPrincipal = new UserPrincipal(savedUser);
//            String emailVerifyMail =
//                    emailService.buildEmailVerifyMail(jwtService.generateToken(userPrincipal));
//            emailService.send(savedUser.getEmail(), AppConstants.VERIFY_EMAIL, emailVerifyMail);
            return savedUser;
        }
        return null;
    }

    @Override
    public User updateUserInfo(UpdateUserInfoDto updateUserInfoDto) {
        User authUser = getAuthenticatedUser();
        if (updateUserInfoDto.getCountryName() != null) {
            Country selectedUserCountry = countryService.getCountryByName(updateUserInfoDto.getCountryName());
            authUser.setCountry(selectedUserCountry);
        }
        mapstructMapperUpdate.updateUserFromUserUpdateDto(updateUserInfoDto, authUser);
        return userRepository.save(authUser);
    }

    @Override
    public User updateEmail(UpdateEmailDto updateEmailDto) {
        User authUser = getAuthenticatedUser();
        String newEmail = updateEmailDto.getEmail();
        String password = updateEmailDto.getPassword();

        if (!newEmail.equalsIgnoreCase(authUser.getEmail())) {
            try {
                User duplicateUser = getUserByEmail(newEmail);
                if (duplicateUser != null) {
                    throw new EmailExistsException();
                }
            } catch (UserNotFoundException e) {
                if (passwordEncoder.matches(password, authUser.getPassword())) {
                    authUser.setEmail(newEmail);
                    authUser.setEmailVerified(false);
                    authUser.setDateLastModified(new Date());
                    User updatedUser = userRepository.save(authUser);
                    UserPrincipal userPrincipal = new UserPrincipal(updatedUser);
//                    String emailVerifyMail =
//                            emailService.buildEmailVerifyMail(jwtService.generateToken(userPrincipal));
//                    emailService.send(updatedUser.getEmail(), AppConstants.VERIFY_EMAIL, emailVerifyMail);
                    return updatedUser;
                } else {
                    throw new InvalidOperationException();
                }
            }
        } else {
            throw new SameEmailUpdateException();
        }
        return null;
    }

    @Override
    public User updatePassword(UpdatePasswordDto updatePasswordDto) {
        User authUser = getAuthenticatedUser();
        if (passwordEncoder.matches(updatePasswordDto.getOldPassword(), authUser.getPassword())) {
            authUser.setPassword(passwordEncoder.encode(updatePasswordDto.getPassword()));
            authUser.setDateLastModified(new Date());
            return userRepository.save(authUser);
        } else {
            throw new InvalidOperationException();
        }
    }

    @Override
    public User verifyEmail(String token) {
        String targetEmail = "";
//                jwtService.getSubjectFromToken(token);
        User targetUser = getUserByEmail(targetEmail);
        targetUser.setEmailVerified(true);
        targetUser.setAccountVerified(true);
        targetUser.setDateLastModified(new Date());
        return userRepository.save(targetUser);
    }

    @Override
    public User updateProfilePhoto(MultipartFile profilePhoto) {
        User targetUser = getAuthenticatedUser();
        if (!profilePhoto.isEmpty() && profilePhoto.getSize() > 0) {
            String uploadDir = environment.getProperty("upload.user.images");
            String oldPhotoName = targetUser.getProfilePhoto();
            String newPhotoName = fileNamingUtil.nameFile(profilePhoto);
            String newPhotoUrl = environment.getProperty("app.root.backend") + File.separator + environment.getProperty("upload.user.images") + File.separator + newPhotoName;
            targetUser.setProfilePhoto(newPhotoUrl);
            try {
                fileUploadUtil.saveNewFile(uploadDir, newPhotoName, profilePhoto);

            } catch (IOException e) {
                throw new RuntimeException();
            }
        }
        return userRepository.save(targetUser);
    }

    @Override
    public User updateCoverPhoto(MultipartFile coverPhoto) {
        User targetUser = getAuthenticatedUser();
        if (!coverPhoto.isEmpty() && coverPhoto.getSize() > 0) {
            String uploadDir = environment.getProperty("upload.user.images");
            String oldPhotoName = targetUser.getCoverPhoto();
            String newPhotoName = fileNamingUtil.nameFile(coverPhoto);
            String newPhotoUrl = environment.getProperty("app.root.backend") + File.separator + environment.getProperty("upload.user.images") + File.separator + newPhotoName;
            targetUser.setCoverPhoto(newPhotoUrl);
            try {
//                if (oldPhotoName == null) {
                fileUploadUtil.saveNewFile(uploadDir, newPhotoName, coverPhoto);
//                } else {
//                    fileUploadUtil.updateFile(uploadDir, oldPhotoName, newPhotoName, coverPhoto);
//                }
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }
        return userRepository.save(targetUser);
    }

    @Override
    public void forgotPassword(String email) {
        try {
            User targetUser = getUserByEmail(email);
            UserPrincipal userPrincipal = new UserPrincipal(targetUser);
            String emailVerifyMail = emailService.buildResetPasswordMail(jwtService.generateToken(userPrincipal));
            emailService.send(targetUser.getEmail(), AppConstants.RESET_PASSWORD, emailVerifyMail);
        } catch (UserNotFoundException ignored) {
        }
    }

    @Override
    public User resetPassword(String token, ResetPasswordDto resetPasswordDto) {
        String targetUserEmail = jwtService.getSubjectFromToken(token);
        User targetUser = getUserByEmail(targetUserEmail);
        targetUser.setPassword(passwordEncoder.encode(resetPasswordDto.getPassword()));
        return userRepository.save(targetUser);
    }

    @Override
    public void deleteUserAccount() {
        User authUser = getAuthenticatedUser();
        String profilePhoto = getPhotoNameFromPhotoUrl(authUser.getProfilePhoto());
        // delete user profile picture from filesystem if exists
        if (profilePhoto != null && profilePhoto.length() > 0) {
            String uploadDir = environment.getProperty("upload.user.images");
            try {
                fileUploadUtil.deleteFile(uploadDir, profilePhoto);
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }
        userRepository.deleteByEmail(authUser.getEmail());
    }

    @Override
    public void followUser(Long userId) {
        User authUser = getAuthenticatedUser();
        if (!authUser.getId().equals(userId)) {
            User userToFollow = getUserById(userId);
            authUser.getFollowingUsers().add(userToFollow);
            authUser.setFollowingCount(authUser.getFollowingCount() + 1);
            userToFollow.getFollowerUsers().add(authUser);
            userToFollow.setFollowerCount(userToFollow.getFollowerCount() + 1);
            userRepository.save(userToFollow);
            userRepository.save(authUser);
        } else {
            throw new InvalidOperationException();
        }
    }

    @Override
    public void unfollowUser(Long userId) {
        User authUser = getAuthenticatedUser();
        if (!authUser.getId().equals(userId)) {
            User userToUnfollow = getUserById(userId);
            authUser.getFollowingUsers().remove(userToUnfollow);
            authUser.setFollowingCount(authUser.getFollowingCount() - 1);
            userToUnfollow.getFollowerUsers().remove(authUser);
            userToUnfollow.setFollowerCount(userToUnfollow.getFollowerCount() - 1);
            userRepository.save(userToUnfollow);
            userRepository.save(authUser);
        } else {
            throw new InvalidOperationException();
        }
    }

    @Override
    public List<UserResponse> getUserSearchResult(String key, Integer page, Integer size) {
        if (key.length() < 3) throw new InvalidOperationException();

        return userRepository.findUsersByName(key, PageRequest.of(page, size)).stream().map(this::userToUserResponse).collect(Collectors.toList());
    }

    @Override
    public List<User> getLikesByPostPaginate(Post post, Integer page, Integer size) {
        return userRepository.findUsersByLikedPosts(post, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "firstName", "lastName")));
    }

    @Override
    public List<User> getLikesByCommentPaginate(Comment comment, Integer page, Integer size) {
        return userRepository.findUsersByLikedComments(comment, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "firstName", "lastName")));
    }

    public final User getAuthenticatedUser() {
        String email = JwtAuthenticationFilter.email;
        return getUserByEmail(email);
    }

    private String getPhotoNameFromPhotoUrl(String photoUrl) {
        if (photoUrl != null) {
            String stringToOmit = environment.getProperty("app.root.backend") + File.separator + environment.getProperty("upload.user.images") + File.separator;
            return photoUrl.substring(stringToOmit.length());
        } else {
            return null;
        }
    }

    private UserResponse userToUserResponse(User user) {
        User authUser = getAuthenticatedUser();
        return UserResponse.builder().user(user).followedByAuthUser(user.getFollowerUsers().contains(authUser)).build();
    }

    @Override
    public AuthResponse login(LoginDto request) {
        try {
            User utilisateur = userRepository.findByEmail(request.getEmail()).orElse(null);

            if (utilisateur == null) {
                return new AuthResponse("Email not found", null);
            }


            if (!passwordEncoder.matches(request.getPassword(), utilisateur.getPassword())) {
                return new AuthResponse("Incorrect password", null);
            }
            UserPrincipal userPrincipal = new UserPrincipal(utilisateur);
            String token = jwtService.generateToken(userPrincipal);

            return new AuthResponse("Login successfu", token);

        } catch (Exception e) {
            return new AuthResponse("Internal error: " + e.getMessage(), null);
        }
    }

    @Override
    public List<UserSimpleDto> searchUsersByNameOrFirstname(String query) {
        List<User> users = userRepository.findUsersByName(query, org.springframework.data.domain.Pageable.unpaged());
        return users.stream().map(u -> new UserSimpleDto(u.getId(), u.getFirstName(), u.getLastName())).collect(Collectors.toList());
    }

    @Override
    public User setUserStatus(Long id, boolean online) {
        User user = getUserById(id);
        user.setOnline(online);
        return userRepository.save(user);
    }

    @Override
    public List<UserChatDto> getAllChatUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "firstName", "lastName")).stream().map(u -> new UserChatDto(u.getId(), u.getFirstName() + " " + u.getLastName(), u.isOnline())).collect(Collectors.toList());
    }
}
