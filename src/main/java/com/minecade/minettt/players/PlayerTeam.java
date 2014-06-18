package com.minecade.minettt.players;

import com.minecade.minettt.main.MineTTT;

//Enum which contains the values for distinguishing between the teams. None
// should only be used on players who are not involved in the game - most
// likely players who joined after the game starts.
public enum PlayerTeam {

	INNOCENT, DETECTIVE, TRAITOR, NONE;
	
	private String name;
	
	private PlayerTeam(){
	    this.name = MineTTT.getPlugin().getMessage("playerteam." + this.name().toLowerCase());
	}

	public String getName(){
	    return this.name;
	}
}