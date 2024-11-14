class MelodyData2R {
  def width
  def pianoroll
  def engine
  def curve1
  def scc
  def cfg

  PitchRestriction restrictionList
  
  MelodyData2R(filename, width, cmxcontrol, pianoroll, cfg, restrictionList) {
    this.width = width
    this.pianoroll = pianoroll
    this.cfg = cfg

    this.restrictionList=restrictionList
    //println(restrictionList.beat2restrictionList(0))

    scc = cmxcontrol.readSMFAsSCC(filename)
    scc.repeat(cfg.INITIAL_BLANK_MEASURES * cfg.BEATS_PER_MEASURE *
	       scc.division,
	       (cfg.INITIAL_BLANK_MEASURES + cfg.NUM_OF_MEASURES) *
	       cfg.BEATS_PER_MEASURE * scc.division, cfg.REPEAT_TIMES - 1)
    def target_part = scc.getFirstPartWithChannel(1)
//    engine = new JamSketchEngineSimple()
    engine = Class.forName(cfg.JAMSKETCH_ENGINE).newInstance()
    engine.restrictionList=this.restrictionList
    //println(engine.restrictionList.beat2restrictionList(0))
    
      //    print("JSEA")
      // for(List<Integer> rList : engine.restrictionList){
      //   print(i+" ")
      //   i++
      //   for (int note  : rList) {
      //       print(note+"_")
      //   }
      //   println("↻")
      // }
      // i=0
    //println(engine.restrictionList)
    engine.init(scc, target_part, cfg)
 //         int i=0
     //      print("JSEA")
      // for(List<Integer> rList : engine.restrictionList){
      //   print(i+" ")
      //   i++
      //   for (int note  : rList) {
      //       print(note+"_")
      //   }
      //   println("↻")
      // }
      // i=0
    resetCurve()

    engine.mr.name2layer.get(engine.OUTLINE_LAYER).calculators.get(0).restrictionList=this.restrictionList
    println(engine.mr.name2layer.get(engine.OUTLINE_LAYER).calculators.get(0).restrictionList.beat2restrictionList(0))
    print("MD2R")

  }

  def resetCurve() {
    curve1 = [null] * width
    engine.resetMelodicOutline()
  }

  def updateCurve(int from, int thru) {
    int nMeas = cfg.NUM_OF_MEASURES
    int div = cfg.DIVISION
    int size2 = nMeas * div
    for (int i in from..thru) {
      if (curve1[i] != null) {
//        double nn = (curve1[i] == null ? null : pianoroll.y2notenum(curve1[i]))
        double nn = pianoroll.y2notenum(curve1[i])
        int ii = i - 100
        int position = (int)(ii * size2 / (curve1.size() - 100))
        if (position >= 0) {
          engine.setMelodicOutline((int)(position / div), position % div, nn)
        }
      }
    }
  }

  // def setRestrictionList(List<List<Integer>> restrictionList){
  //   this.restrictionList=restrictionList
  //   //engine=setRestrictionList(restrictionList)
  //   engine.restrictionList=this.restrictionList
  // }

  def edit(){
    //engine
  }
}	    

/*
import jp.crestmuse.cmx.processing.gui.SimplePianoRoll
import jp.crestmuse.cmx.misc.*
import static jp.crestmuse.cmx.misc.ChordSymbol2.*
class PitchRestriction{
    MelodyData2 melodyData
    PitchRestriction(MelodyData2 melodyData){
        this.melodyData=melodyData
        //
    }
}
*/