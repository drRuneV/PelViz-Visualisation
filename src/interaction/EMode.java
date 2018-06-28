package interaction;


public enum EMode {
	NORMAL,
	MEASURE,
	SELECT;

	final static String[] modeNames= new String[]{"normal","measure","select"};
	
	private EMode() {
		
	}

	public EMode change(int n){
		EMode e= NORMAL;
		switch(n){
		case 0:
			e= NORMAL;
			break;
		case 1:
			e= MEASURE;
			break;
		case 2:
			e= SELECT;
			break;
		}
		
		System.out.println("returning mode: "+e.toString());
		return e;
	}

	
	public EMode next() {
        return ( this.ordinal() + 1 > values().length - 1) ? NORMAL : values()[this.ordinal() + 1];
    }

	
}
