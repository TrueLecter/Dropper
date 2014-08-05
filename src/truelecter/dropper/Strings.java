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
		listDeath.add("$PLAYERNAME$ �������� �����������.");
		listDeath.add("$PLAYERNAME$ ������� �������.");
		listDeath.add("$PLAYERNAME$ ������� ������.");
		listDeath.add("$PLAYERNAME$ ��������.");
		sbName = "&2����";
		addString = "&9SCDropper &7> &e����� &a$PLAYERNAME$ &e������� $POINTS$&a";
		pointsEnds[0] = "����";
		pointsEnds[1] = "����";
		pointsEnds[2] = "����";
		pointsEnds[3] = "����";
		pointsEnds[4] = "�����";
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
