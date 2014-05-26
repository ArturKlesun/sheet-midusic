package Musica;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import Tools.Pointerable;

public class Nota extends Pointerable {
	public static int time = 0;
	int myTime;
	
	public int length = 1;
	public int lengthAccord = -1;
	public int accCislic = 16;
	
	double autoDur = .25; 
	double autoDurAccord = .25; 
	double userDur;
	boolean userDurDef = false;
		
	public Nota retrieve;
	
	public Nota accord;
	boolean isFirst;
	
    public int tune;    
    public int forca;    
    
	public int pos;
    public int okt;
    
    boolean mergeNext = false;
    
    public Nota(int tune){    	    	
        this.tune = tune;             
        seTune(tune);
        forca = 127;
        slog = "";
        if (!bufInited) {
        	bufInit();
        	bufInited = true;        	
        }
    }
    public Nota(int tune, int cislic) {
    	this(tune);
    	durCislic = cislic;
    }
    
    public Nota(int tune, long elapsed){  	    	
        this(tune);
        time += elapsed;
    	myTime = time;
    }
    
    public Nota(int tune, int forca, int cislic, int autoDur){
    	this(tune);
    	
        this.durCislic = cislic;
        this.autoDur = autoDur / 1000.0;
                
    }
    
    public boolean isBemol;

    static int rawToAcad(int midin){
        midin %= 12;
        switch(midin){
            case 11: return 6;  // си
            case 10: return 6;
            case 9: return 5; // ля
            case 8: return 5;
            case 7: return 4; // соль
            case 6: return 4;
            case 5: return 3; // фа
            case 4: return 2; // ми
            case 3: return 2;
            case 2: return 1; // ре
            case 1: return 1;
            case 0: return 0; // до
            default: return -1;
        }
    }
    static int acadToRaw(int pos){
        switch(pos){
            case 6: return 11;  // си
            case 5: return 9;
            case 4: return 7;
            case 3: return 5;
            case 2: return 4;
            case 1: return 2;
            case 0: return 0; // до
            default: return -1;
        }
    }

    @Override
    public String toString() {
        String s = "midi: "+tune+"; pos: "+pos+"; okt: "+okt+"; "+strTune(pos);
        return s;
    }

    private String strTune(int n){
        if (n < 0) {
            n += Integer.MAX_VALUE - Integer.MAX_VALUE%12;
        }
        n %= 12;
        switch(n){
            case 0: return "до";
            case 1: return "ре";
            case 2: return "ми";
            case 3: return "фа";
            case 4: return "соль";
            case 5: return "ля";
            case 6: return "си";
            default: return "ша-бемоль";
        }
    }
    public int acadComp(int midi){
        int oct = midi / 12;
        int pos = rawToAcad(midi);
       
        if (oct > this.okt) return midi;
        else if (oct == this.okt) {
            if (pos > this.pos) return midi;
        }

        return (12*this.okt + Nota.acadToRaw(this.pos) );
    }
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + tune;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Nota other = (Nota) obj;
		if (tune != other.tune)
			return false;
		return true;
	}
	private void seTune(int tu){		
		tune = tu;
		okt = tune/12;
        int tmp = tune;        
        tmp %= 12;
        isBemol = false;
        switch(tmp){
            case 11: pos = 6; break; // си
            case 10: pos = 6; isBemol=true; break;
            case 9: pos = 5; break;
            case 8: pos = 5; isBemol=true; break;
            case 7: pos = 4; break;
            case 6: pos = 4; isBemol=true; break;
            case 5: pos = 3; break;
            case 4: pos = 2; break;
            case 3: pos = 2; isBemol=true; break;
            case 2: pos = 1; break;
            case 1: pos = 1; isBemol=true; break;
            case 0: pos = 0; break; // до
            default: pos = -1; break;
        }
	}
    
	public Nota append(Nota newbie){
		Nota cur = this;
		Nota rez = cur;		
        do {
        	if (cur.tune == newbie.tune) return null; 
        	if (cur.tune < newbie.tune) {
        		int tmp = cur.tune;
        		cur.seTune(newbie.tune);
        		newbie.seTune(tmp);
        	}
        	rez = cur;
        	cur = cur.accord;
        } while (cur != null);
		rez.accord = newbie;
		return newbie;
	}
    
	public void clearAccord(){
		Nota cur = this;
        while (cur.accord != null) {
        	Nota tmp = cur.accord;
        	cur.accord = null;
        	cur = tmp;
        }		
	}
	
	@Override
	public void changeDur(int n, boolean single){
		userDurDef = true;
		if ( (accord != null) && (single == false) ) accord.changeDur(n, false); 
		
		if (durCislic == durZnamen*2) {
			durCislic = durZnamen;
			n = 0;
		}
		if (durCislic < 4) {
			durCislic = 8;
			n = 0;
		}
		while (n > 0){ 
			if (durCislic % 3 == 0) {				
				durCislic += durCislic/3;
			} else {
				durCislic += durCislic/2;
			}
			--n;
		}
		while (n < 0){
			if (durCislic % 3 == 0) {				
				durCislic -= durCislic/3;
			} else {
				durCislic -= durCislic/4;
			}
			++n;
		}
	}
    
    
    
    public int getAccLen(){
    	if (accord != null) return Math.min(durCislic, accord.getAccLen());
    	else return durCislic;
    }

    private static Boolean bufInited = false;
    private static BufferedImage notaImg[] = new BufferedImage[8];    
    static void bufInit() {
    	System.out.println("Эта функция запускается лишь один раз - при создании первого экземпляра класса");
    	    	
    	// Копипаст
    	File notRes[] = new File[8];
        for (int idx = -1; idx<7; ++idx){
        	String str = "out/imgs/" + pow(2, idx) + "_sized.png";
        	notRes[idx+1] = new File(str); 
        }
        for (int idx = 0; idx < 8; ++idx){
        	try {
            	System.out.println( notRes[idx].getCanonicalPath() );
        		if (notRes[idx] != null) notaImg[idx] = ImageIO.read(notRes[idx]);
        	} catch (IOException e) { System.out.println("Ноты не читаются!!! "+idx); }
        }
    	// Заполнить нотаИмг нужными картинками нот
    }
    public BufferedImage getImage() {
    	int idx = (int)(Math.ceil(7 - Math.log(durCislic) / Math.log(2) ));
    	return notaImg[idx];    	
    	// Добавить проверку на выход за границы допустимого  	
    }
    
    
    private static int pow(int n, int k){
    	if (k == 5) return 16;
    	if (k < 0) return 0;
    	if (k==0) return 1;
    	return n*pow(n, k-1);
    }
    
}