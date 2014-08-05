package truelecter.dropper;

import java.util.ArrayList;
import java.util.Random;

public class Strings {
	private ArrayList<String> listDeath = new ArrayList<String>();
	private Random randomGenerator = new Random();
	private String sbName;
	private String addString;
	private String[] pointsEnds = new String[5];

	public Strings(Dropper plugin) {
		loadStrings();
	}

	private void loadStrings() {
		listDeath.add("$PLAYERNAME$ немножко промахнулся.");
		listDeath.add("$PLAYERNAME$ прыгнул неглядя.");
		listDeath.add("$PLAYERNAME$ немного слепой.");
		listDeath.add("$PLAYERNAME$ сломался.");
		sbName = "&2Очки";
		addString = "&9SCDropper &7> &eИгрок &a$PLAYERNAME$ &eполучил $POINTS$&a";
		pointsEnds[0] = "очко";
		pointsEnds[1] = "очка";
		pointsEnds[2] = "очка";
		pointsEnds[3] = "очка";
		pointsEnds[4] = "очков";
	}
	
	public String getDeathMessage(String name) {
		int index = randomGenerator.nextInt(listDeath.size());
		return listDeath.get(index).replace("$PLAYERNAME$", name);
	}

	public String getPointMessage(String name, int points) {
		return addString.replace("$PLAYERNAME$", name).replace(
				"$POINTS$", String.valueOf(points)) + " "
				+ pointsEnds[points - 1];
	}

	public String getSBName() {
		return sbName;
	}

}
