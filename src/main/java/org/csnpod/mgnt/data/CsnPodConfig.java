package org.csnpod.mgnt.data;

import com.google.common.base.MoreObjects;

public class CsnPodConfig {

	public static String adminPhoneNum = "NOT_ASSIGNED_NUMBER";

	public static void showCsnPodConfigValue() {
		System.out.println("Admin Phone Number: "+ adminPhoneNum);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("Admin Phone Number",adminPhoneNum).toString();
	}
}
