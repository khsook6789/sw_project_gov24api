package com.hwn.sw_project.service.userinterest;

import com.hwn.sw_project.dto.interest.*;

import java.util.List;

public interface UserInterestService {
    InterestResponse add(Long userId, AddInterestRequest req);
    void remove(Long userId, String tag);
    List<InterestResponse> list(Long userId);
}
