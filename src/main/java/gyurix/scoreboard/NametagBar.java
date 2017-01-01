package gyurix.scoreboard;

import static gyurix.scoreboard.ScoreboardAPI.id;

public class NametagBar extends ScoreboardBar {

    public NametagBar() {
        super("NB" + id, "NB" + id++, 1);
    }
}

