package me.Cutiemango.LogUploader;

public enum Encounter
{
	// Fotm - 1
	// CM 100
	SKOR("Skorvald the Shattered", 1, -1),
	ARTS("Artsariiv", 1, -1),
	ARKK("Arkk", 1, -1),

	// CM 99
	MAMA("MAMA", 1, -1),
	SIAX("Nightmare Oratuss", 1, -1),
	ENSO("Ensolyss of the Endless Torment", 1, -1),

	// Raids - 2
	// WING 1
	VG("Vale Guardian", 2, 1),
	GORS("Gorseval the Multifarious", 2, 1),
	SAB("Sabetha the Saboteur", 2, 1),

	// WING 2
	SLOTH("Slothasor", 2, 2),
	MATT("Matthias Gabrel", 2, 2),

	// WING 3
	KC("Keep Construct", 2, 3),
	XERA("Xera", 2, 3),

	// WING 4
	CAIRN("Cairn the Indomitable", 2, 4),
	MO("Mursaat Overseer", 2, 4),
	SAM("Samarog", 2,4),
	DEIMOS("Deimos", 2, 4),

	// WING 5
	SH("Soulless Horror", 2, 5),
	DHUUM("Dhuum", 2, 5),

	// WING 6
	CA("Conjured Amalgamate", 2, 6),
	TWINS("Nikare", 2, 6),
	QADIM("Qadim", 2, 6),

	// WING 7
	ADINA("Cardinal Adina", 2, 7),
	SABIR("Cardinal Sabir", 2, 7),
	QPEER("Qadim the Peerless", 2, 7)
	;

	Encounter(String s, int i, int w)
	{
		fileName = s;
		type = i;
		wing = w;
	}

	private String fileName;
	private int type;
	private int wing;

	public String getFileName()
	{
		return fileName;
	}

	public int getType()
	{
		return type;
	}

	public int getWing()
	{
		return wing;
	}

	public boolean isFractal()
	{
		return type == 1;
	}


}
