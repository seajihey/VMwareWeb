package com.vmportal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

	@GetMapping("/deploy-form")
	public String deployForm() {
		return "deploy-form";
	}
}