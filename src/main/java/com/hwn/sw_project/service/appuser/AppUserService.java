package com.hwn.sw_project.service.appuser;

import com.hwn.sw_project.dto.user.*;

public interface AppUserService {
    UserResponse signUp(SignUpRequest req);
    UserResponse get(Long userId);
}
