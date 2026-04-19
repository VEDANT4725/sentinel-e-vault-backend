package com.indore.service;

import com.indore.dto.CreateInviteRequest;
import com.indore.dto.InviteResponse;

import java.util.List;

public interface InviteService {

    void createInvite(CreateInviteRequest request, String userEmail);

    List<InviteResponse> getPendingInvites(String email);
    List<InviteResponse> getSentInvites(String email);

    void acceptInvite(Long inviteId, String userEmail);

    void rejectInvite(Long inviteId, String userEmail);
}
