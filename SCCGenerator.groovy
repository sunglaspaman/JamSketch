import groovy.transform.*
import jp.crestmuse.cmx.inference.*

class SCCGenerator implements MusicCalculator {

  def CFG
  def target_part
  def sccdiv
  def curveLayer
  static def firstMeasure = 0
  
  SCCGenerator(target_part, sccdiv, curveLayer, CFG) {
    this.CFG = CFG
    this.target_part = target_part
    this.sccdiv = sccdiv
    this.curveLayer = curveLayer
  }

    void updated(int measure, int tick, String layer,
		 MusicRepresentation mr) {
      //      def sccdiv = scc.getDivision()
      //def firstMeasure = pianoroll.getDataModel().getFirstMeasure()
      def e = mr.getMusicElement(layer, measure, tick)
      if (!e.rest() && !e.tiedFromPrevious()) {
	//def curvevalue = curve2[measure * CFG.DIVISION + tick]
	def curvevalue =
	  mr.getMusicElement(curveLayer, measure, tick).getMostLikely()
	if (curvevalue != null) {
	  int notenum = getNoteNum(e.getMostLikely(), curvevalue)
	int duration = e.duration() * sccdiv /
	(CFG.DIVISION / CFG.BEATS_PER_MEASURE)
	int onset = ((firstMeasure + measure) * CFG.DIVISION + tick) * sccdiv /
	(CFG.DIVISION / CFG.BEATS_PER_MEASURE)
	//	if (onset > pianoroll.getTickPosition()) {
	  synchronized(this) {
	    //	    def oldnotes =
	    //	      SCCUtils.getNotesBetween(target_part, onset,
	    //				       onset+duration, sccdiv, true, true)
	      //data.target_part.getNotesBetween2(onset, onset+duration)
	    //	      target_part.remove(oldnotes)
	      // edit 2020.03.04
	      target_part.eachnote { note ->
		if (note.onset() < onset && onset <= note.offset()) {
		  target_part.remove(note)
		  target_part.addNoteElement(note.onset(), onset-1,
					     note.notenum(),
					     note.velocity(),
					     note.offVelocity())
		}
		if (onset <= note.onset() && note.offset() <= onset+duration) {
		  target_part.remove(note)
		}
		if (note.onset() < onset+duration &&
		    onset+duration < note.offset()) {
		  //		  note.setOnset(onset+duration)
		}
	      }
	      target_part.addNoteElement(onset, onset+duration, notenum,
					 100, 100)
	      //	  }
	}
	}
      }

      if (CFG.EXPRESSION) {
	def fromTick = (firstMeasure + measure) * CFG.BEATS_PER_MEASURE *
	  CFG.DIVISION
	def thruTick = fromTick + CFG.BEATS_PER_MEASURE * CFG.DIVISION
	expgen.execute(fromTick, thruTick, CFG.DIVISION)
      }
    }

  @CompileStatic
  int getNoteNum(int notename, double neighbor) {
    int best = 0
    for (int i in 0..11) {
      def notenum = i * 12 + notename
      if (Math.abs(notenum - neighbor) < Math.abs(best - neighbor))
	best = notenum
    }
    best
  }
}
