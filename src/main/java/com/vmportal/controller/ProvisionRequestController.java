package com.vmportal.controller;

import com.vmportal.dto.CreateProvisionRequestDto;
import com.vmportal.service.ProvisionRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ProvisionRequestController {

	private final ProvisionRequestService provisionRequestService;

	@GetMapping("/")
	public String showForm(Model model) {
		model.addAttribute("createProvisionRequestDto", new CreateProvisionRequestDto());
		return "form";
	}

	@PostMapping("/requests")
	public String createRequest(@Valid @ModelAttribute CreateProvisionRequestDto createProvisionRequestDto,
		BindingResult bindingResult,
		Model model) {

		if (bindingResult.hasErrors()) {
			return "form";
		}

		provisionRequestService.createRequest(createProvisionRequestDto);
		return "redirect:/requests";
	}

	@GetMapping("/requests")
	public String showRequests(Model model) {
		model.addAttribute("requests", provisionRequestService.getAllRequests());
		return "requests";
	}







}