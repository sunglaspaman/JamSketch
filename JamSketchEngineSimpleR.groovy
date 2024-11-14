import jp.crestmuse.cmx.filewrappers.*

class JamSketchEngineSimpleR extends JamSketchEngineAbstract {
    
  // def musicCalculatorForOutline() {
  //   new NoteSeqGeneratorR(MELODY_LAYER, CHORD_LAYER, cfg.BEATS_PER_MEASURE,
	// 		 cfg.ENT_BIAS, model)
  // }

  PitchRestriction restrictionList

  def musicCalculatorForOutline() {
    new NoteSeqGeneratorR(MELODY_LAYER, CHORD_LAYER, cfg.BEATS_PER_MEASURE,
			 cfg.ENT_BIAS, model)
  }

  def outlineUpdated(measure, tick) {
    // do nothing
  }

  def automaticUpdate() {
    true
  }

  Map<String,Double> parameters() {
    [:]
  }

  Map<String,String> paramDesc() {
    [:]
  }

  void change(int measure, int tick, double nn){
    //println(restrictionList)
    // tick*=cfg.DIVISION
    // tick/=cfg.BEATS_PER_MEASURE
    // int tickt=tick as int
    // measure=x2measure(mx)
    // tick=x2tick(mx)
    // nn=y2notenum(my)
    def e = mr.getMusicElement(MELODY_LAYER, measure, tick)
    //println(e.tiedFromPrevious())
    while (e.prev().tiedFromPrevious()) {
      e = e.prev();
      //println(e.measure() + " " + e.tick())
    }
    //if (!automaticUpdate()) {
      e.suspendUpdate()
    //}
    int intNotenum=nn as int 
    if(restrictionList.beat2restrictionListByTick(tick).get(measure%12).contains(intNotenum%12) ? true :false ){
      e.setEvidence(nn as int %12)
      outlineUpdated(measure, tick)
    }
    //println(x2tick(mouseX))

  }

  def getStartElement(int measure,int tick){
    def e = mr.getMusicElement(MELODY_LAYER, measure, tick)
    while (e.prev().tiedFromPrevious()) {
      e = e.prev();
      //println(e.measure() + " " + e.tick())

    }
          return e
  }

  def getEndElement(int measure,int tick){
    def e = mr.getMusicElement(MELODY_LAYER, measure, tick)
    while (e.next().tiedFromPrevious()) {
      e = e.next();
      //println(e.measure() + " " + e.tick())

    }
          return e
  }

  // def setRestrictionList(List<List<Integer>> restrictionList){
  //   this.restrictionList=restrictionList
  // }

}
