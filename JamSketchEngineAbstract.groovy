import groovy.json.*
import jp.crestmuse.cmx.filewrappers.*
import jp.crestmuse.cmx.processing.*
import jp.crestmuse.cmx.inference.*
import jp.crestmuse.cmx.misc.*
import static jp.crestmuse.cmx.misc.ChordSymbol2.*

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import jp.crestmuse.cmx.processing.gui.SimplePianoRoll
 


abstract class JamSketchEngineAbstract implements JamSketchEngine {
  MusicRepresentation mr
  CMXController cmx
  def cfg
  def model
  def scc
  def expgen = null
  static String OUTLINE_LAYER = "curve"
  static String MELODY_LAYER = "melody"
  static String CHORD_LAYER = "chord"

  PitchRestriction[] restrictionList
  
  void init(SCC scc, SCC.Part target_part, def cfg) {
    // print("JSEA1234567")
    //   int fuck=0
    //   for(List<Integer> rList : restrictionList){
    //     print(fuck+" ")
    //     fuck++
    //     for (int note  : rList) {
    //         print(note+"_")
    //     }
    //     println("↻")
    //   }
    //   fuck=0
    this.scc = scc
    this.cfg = cfg
    def json = new JsonSlurper()
    model = json.parseText((new File(cfg.MODEL_FILE)).text)
    cmx = CMXController.getInstance()
    mr = cmx.createMusicRepresentation(cfg.NUM_OF_MEASURES,
                                       cfg.DIVISION)
    mr.addMusicLayerCont(OUTLINE_LAYER)
    mr.addMusicLayer(MELODY_LAYER, (0..11) as int[])
    mr.addMusicLayer(CHORD_LAYER,
                     [C, F, G] as ChordSymbol2[],	// temporary
                     cfg.DIVISION)
    cfg.chordprog.eachWithIndex{ c, i ->
      mr.getMusicElement(CHORD_LAYER, i, 0).setEvidence(c)
    }
    if (cfg.EXPRESSION) {
       expgen = new ExpressionGenerator()
       expgen.start(scc.getFirstPartWithChannel(1),
	            getFullChordProgression(), cfg.BEATS_PER_MEASURE)
    }
    def sccgen = new SCCGenerator(target_part, scc.division,
    OUTLINE_LAYER, expgen, cfg)
    mr.addMusicCalculator(MELODY_LAYER, sccgen)
    mr.addMusicCalculator(OUTLINE_LAYER,
                         musicCalculatorForOutline())
    //MusicCalculator nsg=musicCalculatorForOutline()
    // for (MusicCalculator c : mr.name2layer.get(OUTLINE_LAYER).calculators){
    //     c.restrictionList=restrictionList
    // // print("JSEA")
    // // int i=0
    // //   for(List<Integer> rList : c.restrictionList){
    // //     print(i+" ")
    // //     i++
    // //     for (int note  : rList) {
    // //         print(note+"_")
    // //     }
    // //     println("↻")
    // //   }
    // //   i=0
    // //print(c.w1)
    // }

    //   print("JSEA########")
    //   int i=0
    //   for(List<Integer> rList : restrictionList){
    //     print(i+" ")
    //     i++
    //     for (int note  : rList) {
    //         print(note+"_")
    //     }
    //     println("↻")
    //   }
    //   i=0

    //   // nsg.restrictionList=restrictionList
    //   // print("JSEA======")
    //   // //int i=0
    //   // for(List<Integer> rList : nsg.restrictionList){
    //   //   print(i+" ")
    //   //   i++
    //   //   for (int note  : rList) {
    //   //       print(note+"_")
    //   //   }
    //   //   println("↻")
    //   // }
    //   // i=0
    
    // mr.addMusicCalculator(OUTLINE_LAYER,nsg)

    // print("JSEA")
    // int i=0
    //   for(List<Integer> rList : calcl.restrictionList){
    //     print(i+" ")
    //     i++
    //     for (int note  : rList) {
    //         print(note+"_")
    //     }
    //     println("↻")
    //   }
    //   i=0
  }

  def getFullChordProgression() {
    [NON_CHORD] * cfg.INITIAL_BLANK_MEASURES + cfg.chordprog * cfg.REPEAT_TIMES
  }

  abstract def musicCalculatorForOutline()

  void setMelodicOutline(int measure, int tick, double value) {
    def e = mr.getMusicElement(OUTLINE_LAYER, measure, tick)
    if (!automaticUpdate()) {
      e.suspendUpdate()
    }
    //println(value+"###################")
    e.setEvidence(value)
    outlineUpdated(measure, tick)
    //println(value)
    
    //change(1, 1)
  }

  double getMelodicOutline(int measure, int tick) {
    mr.getMusicElement(OUTLINE_LAYER, measure, tick).
      getMostLikely()
  }

  abstract def outlineUpdated(measure, tick)

  abstract def automaticUpdate()
    
  void resetMelodicOutline() {
    (0..<cfg.NUM_OF_MEASURES).each { i ->
      (0..<cfg.DIVISION).each { j ->
	mr.getMusicElement(OUTLINE_LAYER, i, j).
          setEvidence(Double.NaN)
      }
    }

    

  }

  void setFirstMeasure(int num) {
    SCCGenerator.firstMeasure = num
  }

  ChordSymbol2 getChord(int measure, int tick) {
    mr.getMusicElement(CHORD_LAYER, measure, tick).
      getMostLikely()
  }

  // Set<String> getChordAsInt(int measure, int tick){
  //   Set<String> css
  //   ChordSymbol cs=mr.getMusicElement(CHORD_LAYER, measure, tick).
  //     getMostLikely()
  //     for (NoteSymbol c_note  : cs.notes()) {
  //       print(c_note)
  //       css
  //     }
  // }


  void change(int measure, int tick, double nn){
    // // tick*=cfg.DIVISION
    // // tick/=cfg.BEATS_PER_MEASURE
    // // int tickt=tick as int
    // // measure=x2measure(mx)
    // // tick=x2tick(mx)
    // // nn=y2notenum(my)
    // def e = mr.getMusicElement(MELODY_LAYER, measure, tick)
    // //println(e.tiedFromPrevious())
    // if(e.prev()!=null){
    //   while (e.prev().tiedFromPrevious()) {
    //     e = e.prev();
    //   //println(e.measure() + " " + e.tick())
    //   }
    // }
    // //if (!automaticUpdate()) {
    //   e.suspendUpdate()
    // //}
    // if(restrictionList[restrictionNum].tick2restrictionList(tick).get(measure%12).contains(intNotenum%12) ? true :false ){
    //   println(restrictionList[restrictionNum].tick2restrictionList(tick).get(measure%12).contains(intNotenum%12))
    //   e.setEvidence(nn as int %12)
    //   outlineUpdated(measure, tick)
    // }
    // //println(x2tick(mouseX))

  }

  double getNotenum(int measure, int tick){
    mr.getMusicElement(MELODY_LAYER, measure, tick).getMostLikely()
  }

  // def setRestrictionList(List<List<Integer>> restrictionList){
  //   this.restrictionList=restrictionList
  // }

  
}

//x2measure(mouseX)pianoroll.y2notenum(mouseY)