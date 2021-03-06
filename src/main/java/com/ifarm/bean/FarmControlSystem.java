package com.ifarm.bean;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "farm_control_system")
public class FarmControlSystem {
	@Id
	private Integer systemId;
	private Integer farmId;
	private String systemCode;
	private String systemType;
	private String systemTypeCode;
	private String systemDistrict;
	private String systemNo;
	private String systemDescription;
	private String systemLocation;
	private Timestamp systemCreateTime;

	public void setSystemId(Integer systemId) {
		this.systemId = systemId;
	}

	public Integer getSystemId() {
		return systemId;
	}

	public void setFarmId(Integer farmId) {
		this.farmId = farmId;
	}

	public Integer getFarmId() {
		return farmId;
	}

	public String getSystemNo() {
		return systemNo;
	}

	public void setSystemNo(String systemNo) {
		this.systemNo = systemNo;
	}

	public String getSystemCode() {
		return systemCode;
	}

	public void setSystemCode(String systemCode) {
		this.systemCode = systemCode;
	}

	public void setSystemType(String systemType) {
		this.systemType = systemType;
	}

	public String getSystemType() {
		return systemType;
	}

	public String getSystemTypeCode() {
		return systemTypeCode;
	}

	public void setSystemTypeCode(String systemTypeCode) {
		this.systemTypeCode = systemTypeCode;
	}

	public void setSystemDistrict(String systemDistrict) {
		this.systemDistrict = systemDistrict;
	}

	public String getSystemDistrict() {
		return systemDistrict;
	}

	public void setSystemLocation(String systemLocation) {
		this.systemLocation = systemLocation;
	}

	public String getSystemLocation() {
		return systemLocation;
	}

	public String getSystemDescription() {
		return systemDescription;
	}

	public void setSystemDescription(String systemDescription) {
		this.systemDescription = systemDescription;
	}

	public void setSystemCreateTime(Timestamp systemCreateTime) {
		this.systemCreateTime = systemCreateTime;
	}

	public Timestamp getSystemCreateTime() {
		return systemCreateTime;
	}

}
