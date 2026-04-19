package com.indore.controller;

import com.indore.dto.CreateInviteRequest;
import com.indore.dto.InviteResponse;
import com.indore.service.InviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invites")
@RequiredArgsConstructor
public class InviteController {

    private final InviteService inviteService;

    @PostMapping
    public String createInvite(@RequestBody CreateInviteRequest request, Authentication authentication){
        String userEmail = authentication.getName().toLowerCase(); // Normalize

        // Normalize the invitee's email right when it's received
        if (request.getEmail() != null) {
            request.setEmail(request.getEmail().toLowerCase());
        }

        inviteService.createInvite(request, userEmail);
        return "Invite sent successfully";
    }

    @GetMapping("/pending")
    public List<InviteResponse> getPendingInvites(Authentication authentication){
        // Ensure the logged-in user's email is lowercase before querying the DB
        String email = authentication.getName().toLowerCase();
        return inviteService.getPendingInvites(email);
    }

    @GetMapping("/sent")
    public List<InviteResponse> getSentInvites(Authentication authentication){
        String email = authentication.getName().toLowerCase();
        return inviteService.getSentInvites(email);
    }

    @PostMapping("/{id}/accept")
    public String acceptInvite(@PathVariable Long id, Authentication authentication){
        inviteService.acceptInvite(id, authentication.getName().toLowerCase());
        return "Invite accepted";
    }

    @PostMapping("/{id}/reject")
    public String rejectInvite(@PathVariable Long id, Authentication authentication){
        inviteService.rejectInvite(id, authentication.getName().toLowerCase());
        return "Invite rejected";
    }
}
