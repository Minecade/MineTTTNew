package src.main.java.de.orion304.ttt.minecade;

import java.sql.Timestamp;

public class MinecadeAccount {

	private String username;

	private boolean admin;

	private boolean gm;

	private boolean cm;

	private boolean vip;

	private boolean youtuber;

	private long butterCoins;

	private String pet;

	private Timestamp vipPassDate;

	private int vipPassDialyAttemps;

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the admin
	 */
	public boolean isAdmin() {
		return admin;
	}

	/**
	 * @param admin
	 *            the admin to set
	 */
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	/**
	 * @return the gm
	 */
	public boolean isGm() {
		return gm;
	}

	/**
	 * @param gm
	 *            the gm to set
	 */
	public void setGm(boolean gm) {
		this.gm = gm;
	}

	/**
	 * @return the cm
	 */
	public boolean isCm() {
		return cm;
	}

	/**
	 * @param cm
	 *            the cm to set
	 */
	public void setCm(boolean cm) {
		this.cm = cm;
	}

	/**
	 * @return the vip, if a user is a youtuber, automatically is vip.
	 */
	public boolean isVip() {
		return youtuber || vip;
	}

	/**
	 * @param vip
	 *            the vip to set
	 */
	public void setVip(boolean vip) {
		this.vip = vip;
	}

	/**
	 * @return the butterCoins
	 */
	public long getButterCoins() {
		return butterCoins;
	}

	/**
	 * @param butterCoins
	 *            the butterCoins to set
	 */
	public void setButterCoins(long butterCoins) {
		this.butterCoins = butterCoins;
	}

	/**
	 * @return the youtuber
	 */
	public boolean isYoutuber() {
		return youtuber;
	}

	/**
	 * @param youtuber
	 *            the youtuber to set
	 */
	public void setYoutuber(boolean youtuber) {
		this.youtuber = youtuber;
	}

	/**
	 * @return the pet
	 */
	public String getPet() {
		return pet;
	}

	/**
	 * @param pet
	 *            the pet to set
	 */
	public void setPet(String pet) {
		this.pet = pet;
	}

	/**
	 * @return the vipPassDate
	 */
	public Timestamp getVipPassDate() {
		return vipPassDate;
	}

	/**
	 * @param vipPassDate
	 *            the vipPassDate to set
	 */
	public void setVipPassDate(Timestamp vipPassDate) {
		this.vipPassDate = vipPassDate;
	}

	/**
	 * @return the vipPassDialyAttemps
	 */
	public int getVipPassDailyAttemps() {
		return vipPassDialyAttemps;
	}

	/**
	 * @param vipPassDialyAttemps
	 *            the vipPassDialyAttemps to set
	 */
	public void setVipPassDailyAttemps(int vipPassDialyAttemps) {
		this.vipPassDialyAttemps = vipPassDialyAttemps;
	}
}
