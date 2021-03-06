package com.ifarm.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ifarm.bean.FarmControlSystem;
import com.ifarm.bean.FarmWFMControlSystem;
import com.ifarm.redis.util.FarmControlSystemTypeUtil;
import com.ifarm.service.FarmControlSystemService;
import com.ifarm.service.FarmControlSystemWFMService;

@RestController
@RequestMapping("farmControlSystem")
public class FarmControlSystemController {
	@Autowired
	private FarmControlSystemWFMService farmControlSystemWFMService;

	@Autowired
	private FarmControlSystemService farmControlSystemService;

	@Autowired
	private FarmControlSystemTypeUtil farmControlSystemTypeUtil;

	@RequestMapping("type")
	public String farmControlSystemType() {
		return farmControlSystemTypeUtil.farmControlSystemType().toString();
	}

	@RequestMapping("terminalType")
	public String farmControlSystemTerminalType(String controlType) {
		return farmControlSystemTypeUtil.farmControlSystemTerminal(controlType).toString();
	}

	@RequestMapping("wfm/terminalType")
	public String wfmFarmControlSystemTerminalType(FarmWFMControlSystem fControlSystem) {
		return farmControlSystemWFMService.farmControlSystemWFMTerimal(fControlSystem).toString();
	}

	@RequestMapping("addition")
	public String controlSystemAddition(FarmControlSystem farmControlSystem) {
		return farmControlSystemService.saveControlSystem(farmControlSystem);
	}

	@RequestMapping("wfm/addition")
	public String wfmControlSystemAddition(FarmWFMControlSystem farmWFMControlSystem) {
		return farmControlSystemWFMService.saveControlSystem(farmWFMControlSystem);
	}
}
