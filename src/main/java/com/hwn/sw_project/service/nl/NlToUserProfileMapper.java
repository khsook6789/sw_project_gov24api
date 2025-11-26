package com.hwn.sw_project.service.nl;

import com.hwn.sw_project.dto.gov24.UserProfile;
import com.hwn.sw_project.dto.nl.NlParsedQuery;

import java.util.List;

public class NlToUserProfileMapper {
    public static UserProfile toUserProfile(NlParsedQuery q) {
        List<String> flags = q.specialFlags();
        if (flags == null || flags.isEmpty()) {
            flags = List.of("해당사항없음");
        }

        return new UserProfile(
                q.age(),
                q.gender(),
                q.incomeBracket(),
                flags,
                q.studentStatus(),
                q.employmentStatus(),
                q.industry(),
                q.category()
        );
    }
}
