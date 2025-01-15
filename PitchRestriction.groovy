import controlP5.ControlP5
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import jp.crestmuse.cmx.filewrappers.SCCDataSet
import jp.crestmuse.cmx.processing.gui.SimplePianoRoll
import jp.crestmuse.cmx.misc.*
import static jp.crestmuse.cmx.misc.ChordSymbol2.*
import java.lang.reflect.Method;

class PitchRestriction {

    def cfg
    List<List<Integer>> restrictionListDown=[]
    List<List<Integer>> restrictionListUp=[]
    MelodyData2R melodyData
    int divisionOfBeats
    int toStrong
    int tickSet
    int step
    List<Integer> instep = []

    PitchRestriction(){
    //
    }

    void setData(cfg){
        this.cfg=cfg
        divisionOfBeats=cfg.DIVISION/cfg.BEATS_PER_MEASURE
    }

    void setRestrictionListStrong(){
        for(int i=0;i<cfg.NUM_OF_MEASURES;i++){
            restrictionListDown.add(chordPitch(i))
            restrictionListUp.add(keyMajorScale(i))
        }
        tickSet=divisionOfBeats
        toStrong=2
        //println(restrictionListDown)
        //println(restrictionListUp)
    }

    void setRestrictionListMedium(){
        for(int i=0;i<cfg.NUM_OF_MEASURES;i++){
            restrictionListDown.add(keyMajorScale(i))
            restrictionListUp.add(keyMajorScale(i))
        }
        tickSet=divisionOfBeats
        toStrong=2
        //println(restrictionListDown)
        //println(restrictionListUp)
    }

    void setRestrictionListWeak(){
        for(int i=0;i<cfg.NUM_OF_MEASURES;i++){
            restrictionListDown.add(keyMajorScale(i))
            restrictionListUp.add(noRestrinction(i))
        }
        tickSet=divisionOfBeats
        toStrong=2
        //println(restrictionListDown)
        //println(restrictionListUp)
    }

    void setRestrictionListNone(){
        for(int i=0;i<cfg.NUM_OF_MEASURES;i++){
            restrictionListDown.add(noRestrinction(i))
            restrictionListUp.add(noRestrinction(i))
        }
        tickSet=divisionOfBeats
        toStrong=2
        //println(restrictionListDown)
        //println(restrictionListUp)
    }

    List<List<Integer>> beat2restrictionList(int beat){
        if(beat%2==0){
            return restrictionListDown
        } else {
            return restrictionListUp
        }
    }

    List<List<Integer>> beat2restrictionListByTick(int tick){
        int beat = tick / divisionOfBeats as int
        return beat2restrictionList(beat)
    }
    List<List<Integer>> tick2restrictionList(int tick){
        /* groovylint-disable-next-line DuplicateNumberLiteral */
        // if (((tick / divisionOfBeats) as int ) % toStrong == 0 && tick % (tickSet) == 0) {
        //     return restrictionListDown
        // } else {
        //     return restrictionListUp
        // }
        if (((tick / divisionOfBeats) as int ) % toStrong == 0 && tick % (tickSet) == 0) {
            return restrictionListDown
        } else {
            return restrictionListUp
        }
    }
    
    List<Integer> chordPitch(i){
        List<Integer> pitchList =[]
        ChordSymbol cs= cfg.chordprog.get(i)
        for (NoteSymbol c_note  : cs.notes()) {
            pitchList.add(c_note.number())
            //print(c_note.number()+" ")
        }
        //println();

        return pitchList
    }
    
    List<Integer> keyMajorScale(i){
        List<Integer> pitchList =[]
        List<Integer> ScaleAdd=[2,2,1,2,2,2,1]
        //NoteSymbol mkey = cfg.key
        //int base=key.getInstance(key).number()
        // int baseNotenumber=base
        int key = cfg.key
        int baseNotenumber=key
        for(int j=0;j<7;j++){
            int addScale=ScaleAdd.get(j)
            pitchList.add(baseNotenumber)
            baseNotenumber+=addScale
        }
        return pitchList
    }

    List<Integer> noRestrinction(i){
        List<Integer> pitchList =[]
        for(int j=0;j<12;j++){
            pitchList.add(j)
        }
        return pitchList
    }

}