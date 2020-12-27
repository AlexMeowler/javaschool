package org.retal.controller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class Error403Controller {
	@GetMapping("/403")
	public String accessDeniedUnnamed() {
		return "error403";
	}

	@GetMapping("/403/{name}")
	public RedirectView accessDeniedNamed(@PathVariable String name, RedirectAttributes redir) {
		RedirectView redirectView = new RedirectView("/403", true);
		if (name != null) {
			log.info("access denied, name = " + name);
			redir.addFlashAttribute("username", name);
		}
		return redirectView;
	}

	private static final Logger log = Logger.getLogger(Error403Controller.class);
}
