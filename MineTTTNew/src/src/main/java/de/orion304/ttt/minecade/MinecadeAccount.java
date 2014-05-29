package src.main.java.de.orion304.ttt.minecade;

import java.sql.Timestamp;
import java.util.UUID;

public class MinecadeAccount {

	private UUID uuid;

	private boolean admin;

	private boolean gm;

	private boolean cm;

	private boolean vip;

	private boolean youtuber;

	private boolean titan;

	private boolean owner;

	private boolean dev;

	private long butterCoins;

	private String pet;

	private Timestamp vipPassDate;

	private int vipPassDialyAttemps;

	/**
	 * @return the butterCoins
	 */
	public long getButterCoins() {
		return this.butterCoins;
	}

	/**
	 * @return the pet
	 */
	public String getPet() {
		return this.pet;
	}

	/**
	 * @return the username
	 */
	public UUID getUUID() {
		return this.uuid;
	}

	/**
	 * @return the vipPassDialyAttemps
	 */
	public int getVipPassDailyAttemps() {
		return this.vipPassDialyAttemps;
	}

	/**
	 * @return the vipPassDate
	 */
	public Timestamp getVipPassDate() {
		return this.vipPassDate;
	}

	/**
	 * @return the admin
	 */
	public boolean isAdmin() {
		return this.admin;
	}

	/**
	 * @return the cm
	 */
	public boolean isCm() {
		return this.cm;
	}

	public boolean isDev() {
		return this.dev;
	}

	/**
	 * @return the gm
	 */
	public boolean isGm() {
		return this.gm;
	}

	public boolean isOwner() {
		return this.owner;
	}

	public boolean isTitan() {
		return this.titan;
	}

	/**
	 * @return the vip, if a user is a youtuber, automatically is vip.
	 */
	public boolean isVip() {
		return this.youtuber || this.vip;
	}

	/**
	 * @return the youtuber
	 */
	public boolean isYoutuber() {
		return this.youtuber;
	}

	/**
	 * @param admin
	 *            the admin to set
	 */
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	/**
	 * @param butterCoins
	 *            the butterCoins to set
	 */
	public void setButterCoins(long butterCoins) {
		this.butterCoins = butterCoins;
	}

	/**
	 * @param cm
	 *            the cm to set
	 */
	public void setCm(boolean cm) {
		this.cm = cm;
	}

	public void setDev(boolean dev) {
		this.dev = dev;
	}

	/**
	 * @param gm
	 *            the gm to set
	 */
	public void setGm(boolean gm) {
		this.gm = gm;
	}

	public void setOwner(boolean owner) {
		this.owner = owner;
	}

	/**
	 * @param pet
	 *            the pet to set
	 */
	public void setPet(String pet) {
		this.pet = pet;
	}

	public void setTitan(boolean titan) {
		this.titan = titan;
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void setUUID(String uuid) {
		setUUID(UUID.fromString(uuid));
	}

	/**
	 * 
	 * @param uuid
	 *            the uuid to set
	 * 
	 */
	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}

	/**
	 * @param vip
	 *            the vip to set
	 */
	public void setVip(boolean vip) {
		this.vip = vip;
	}

	/**
	 * @param vipPassDialyAttemps
	 *            the vipPassDialyAttemps to set
	 */
	public void setVipPassDailyAttemps(int vipPassDialyAttemps) {
		this.vipPassDialyAttemps = vipPassDialyAttemps;
	}

	/**
	 * @param vipPassDate
	 *            the vipPassDate to set
	 */
	public void setVipPassDate(Timestamp vipPassDate) {
		this.vipPassDate = vipPassDate;
	}

	/**
	 * @param youtuber
	 *            the youtuber to set
	 */
	public void setYoutuber(boolean youtuber) {
		this.youtuber = youtuber;
	}

	@Override
	public String toString() {
		return "MinecadeAccount [uuid=" + this.uuid + ", admin=" + this.admin
				+ ", gm=" + this.gm + ", cm=" + this.cm + ", vip=" + this.vip
				+ ", youtuber=" + this.youtuber + ", titan=" + this.titan
				+ ", owner=" + this.owner + ", dev=" + this.dev
				+ ", butterCoins=" + this.butterCoins + ", pet=" + this.pet
				+ ", vipPassDate=" + this.vipPassDate
				+ ", vipPassDialyAttemps=" + this.vipPassDialyAttemps + "]";
	}
}
