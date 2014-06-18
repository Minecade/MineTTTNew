package com.minecade.minettt.main;

public enum GameState {

    OFF, GAME_RUNNING, GAME_PREPARING, CELEBRATIONS;

    private String name;

    private GameState(){
        this.name = MineTTT.getPlugin().getMessage("gamestate." + this.name().toLowerCase());
    }

    public String getName(){
        return this.name;
    }
}
