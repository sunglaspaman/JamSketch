import groovy.transform.*
import jp.crestmuse.cmx.inference.*
import jp.crestmuse.cmx.inference.models.*
import java.util.*
import jp.crestmuse.cmx.misc.*  

class NoteSeqGeneratorR implements MusicCalculator {

  String noteLayer, chordLayer  
  Map<String,List<Double>> trigram
  List<List<Double>> bigram
  Map<String,List<Double>> chord_beat_dur_unigram
  double entropy_mean
  double w1 = 0.5 
  double w2 = 0.8 
  double w3 = 1.2 
  double w4 = 2.0 
  //double w5 = 1.0 
  double w6 = 1.0
  int beatsPerMeas
  double entropy_bias
  double RHYTHM_THRS = 0.1
  def RHYTHM_WEIGHTS = [1, 0.2, 0.4, 0.8, 0.2, 0.4,
			1, 0.2, 0.4, 0.8, 0.2, 0.4];
  
  PitchRestriction[] restrictionList
  int restrictionNum=0
  //List<Integer> restr =Arrays.asList(0,4,7)
  //List<Integer> restr2 =Arrays.asList(2,7,11)
  // def cfg
  // List<List<Integer>> restr
  // List<Integer> restrAdd
  // ChordSymbol cs
  
  NoteSeqGeneratorR(noteLayer, chordLayer, beatsPerMeas, entropy_bias, model) {
    this.noteLayer = noteLayer
    this.chordLayer = chordLayer
    this.beatsPerMeas = beatsPerMeas  
    this.entropy_bias = entropy_bias
    trigram = model.trigram
    bigram = model.bigram
    chord_beat_dur_unigram = model.chord_beat_dur_unigram
    entropy_mean = model.entropy.mean

    //this.restrictionList=restrictionList
    //this.cfg=cfg
    // for(int i=0;i<8;i++){
    //   for(List<Integer> ri : restr){
    //     for(ChodeSymbol ch : cfg.chordprog){
          
    //     }
    //   }
    // for(int i=0;i<3;i++){
    // restrList.add(restr)
    // restrList.add(restr2)
    // }
    // restrList.add(restr)
    // restrList.add(restr)
    // restrList.add(restr2)
    // restrList.add(restr2)
    // restrList.add(restr)
    // restrList.add(restr)



    
     }

  @CompileStatic
  Object prev(MusicElement e, int rep, Object ifnull) {
    if (e == null) {
      ifnull
    } else if (rep == 0) {
      e.mostLikely  
    } else {
      prev(e.prev(), rep-1, ifnull)
    }
  }

  @CompileStatic
  void updated(int measure, int tick, String layer,
	      MusicRepresentation mr) { 
          //
              //print("JSEA")
      //print("tick:"+tick+" ")

    MusicElement e_curve = mr.getMusicElement(layer, measure, tick) 
    double value = e_curve.getMostLikely() as double  
    if (!Double.isNaN(value)) {
      MusicElement e_melo = mr.getMusicElement(noteLayer, measure, tick)
      boolean b = decideRhythm(value,
	             (Double)prev(e_curve, 1, Double.NaN), 
                     tick, e_melo, mr)
      if (!b) {
        int prev1 = (Integer)prev(e_melo, 1, -1)
	int prev2 = (Integer)prev(e_melo, 2, -1)
      ChordSymbol2 c = mr.getMusicElement(chordLayer, measure, tick).
      getMostLikely() as ChordSymbol2
      List<Double> scores = []
      List<Integer> prevlist = []
      boolean inRestriction
      for (int i = 0; i < tick; i++) {
	prevlist.add(
	  mr.getMusicElement(noteLayer, measure, i).getMostLikely() as Integer)
      }
      (0..11).each { i ->
	double value12 = value - (int)(value / 12) * 12
	double simil = -Math.log((value12 - i) * (value12 - i))
	double logtrigram = calcLogTrigram(i, prev1, prev2)
	double logchord = calcLogChordBeatUnigram(i, c, tick, beatsPerMeas,
						  e_melo.duration(), mr)
	double entdiff = calcEntropyDiff(i, prevlist)

  double inRes;
  // double harmony 
  // harmony = calcHarmony(mr.getMusicElement(chordLayer, measure, tick),e_melo)
  // if(calcHarmony(mr.getMusicElement(chordLayer, measure, tick),e_melo)==1){
  //   harmony=-2147483647;  //provisional value
  // }

  //int nn_of_e_melo=e_melo.getMostLikely();
  // for(int ir : restr){
  //   if(i%12==ir){
  //     cnt++
  //   }
  // }
  inRestriction=true
  //println("measure="+measure+"restrictionNum="+restrictionNum)
  //println(restrictionList[restrictionNum].tick2restrictionList(tick))
  inRestriction=(restrictionList[restrictionNum].tick2restrictionList(tick).get(measure%12).contains(i) ? true :false )

  //ir=(restr.contains(i) ? true :false )
  // for(int j : restr){
  //   print(i+" "+j+" "+ir)
  //   println();
  // }

  //println("simil:${simil},logtrigram:${logtrigram},logchord:${logchord},entdiff:${entdiff}")
  if(!inRestriction){
      //scores[i]*=2
      inRes=-10000
    }

	// scores[i] = w1 * simil + w2 * logtrigram + w3 * logchord +
	//   w4 * (-entdiff) +w6*inRes//+w5 * harmony //選ばれる確率を求める計算式
  //   println(scores[i])
  scores[i] = w1 * simil  +w6*inRes//+w5 * harmony //選ばれる確率を求める計算式
    //println(scores[i])
    
	
      }
      //println("ir="+inRestriction)
      // if(!ir){
      //   e_melo.setEvidence(0)
      // } else {
      
        e_melo.setEvidence(argmax(scores))
      //}
      
      }
    }
  }
        

  boolean decideRhythm(value, prev, tick, e, mr) {
    e.setTiedFromPrevious(false)
    if (!Double.isNaN(value) && !Double.isNaN(prev)) {
      double score = Math.abs(value - prev) * RHYTHM_WEIGHTS[tick]
      if (score < RHYTHM_THRS) {
	e.setTiedFromPrevious(true)
	true
      }
    }
    false
  }

  // double calcHarmony(MusicElement chmr,MusicElement nomr){
  //   ChordSymbol c=chmr.getMostLikely();
  //   int n=nomr.getMostLikely();
  //   int find1=0;
  //   int find2=0;
  //   double penal=0;
  //   for (NoteSymbol c_note : c.notes()) {
  //     //println("abs(n-c_note.number())==1= ${Math.abs(n-c_note.number())==1}")
  //     //println("abs(n-c_note.number())==2= ${Math.abs(n-c_note.number())==2}")
  //     if (Math.abs(n-c_note.number())==1){
  //       find1++
  //       //println("find1=${find1}")
  //     }
  //     if (Math.abs(n-c_note.number())==2){
  //       find2++
  //       //println("find2=${find2}")
  //     }
  //   }
  //   //println("last find=${find1},${find2}")
  //   if(find1!=0){
  //     penal-=1.5
  //   }
  //   if(find2!=0){
  //     penal-=1.0
  //   }
  //   return penal
  // }

  @CompileStatic
  double calcLogTrigram(int number, int prev1, int prev2) { //1つと2つ前の音からの遷移確率？を求める関数
    if (prev2 == -1) {
      if (prev1 == -1) {
        0
      } else {
        println("      " + prev1)
	      println("      " + number)
	      println(bigram)
	      println(bigram[prev1][number])
        Math.log(bigram[prev1][number])
      }
    } else {
      String key = prev2 + "," + prev1
      if (trigram.containsKey(key)) {
        Math.log(trigram[key][number])
      } else {
        Math.log(0.001 as double)
      }
    }
  }
  
  @CompileStatic
  double calcLogChordBeatUnigram(int number, ChordSymbol2 chord,
				 int tick, int beatsPerMeas, int duration,
				 MusicRepresentation mr) {
    String s_chord = chord.toString()
    int div4 = mr.getDivision() / beatsPerMeas as int
    String s_beat = (tick == 0 ? "head" : 
		     (tick % div4 == 0 ? "on" : "off"))
    String s_dur = (duration >= 2 * div4 ? "long" :
		  (duration >= div4 ? "mid" : "short"))
    String key = s_chord + "_" + s_beat + "_" + s_dur
    Math.log(chord_beat_dur_unigram[key][number] + 0.001 as double) as double
  }

  @CompileStatic
  double calcEntropyDiff(int number, List<Integer> prevlist) {
    double entropy = calcEntropy(number, prevlist)
    double entdiff = entropy - entropy_mean - entropy_bias
    entdiff * entdiff
  }
  
  @CompileStatic
  double calcEntropy(int number, List<Integer> prevlist) {
    int[] freq = [0] * 12
    int sum = 0
    prevlist.each {
      freq[it] += 1
      sum++;
    }
    freq[number] += 1
    sum++;
    double entropy = 0.0
    (0..11).each { i ->
      if (freq[i] > 0) {
	double p = (double)freq[i] / sum
	entropy += -Math.log(p) * p / Math.log(2)
      }
    }
    entropy
  }
  
  @CompileStatic
  int argmax(List<Double> list) {
    double max = list[0]
    int index = 0
    list.eachWithIndex{x, i ->
      if (x > max) {
	max = x
	index = i
      }
    }
    index
  }

  // def setRestrictionList(List<List<Integer>> restrictionList){
  //   this.restrictionList=restrictionList
  // }
}
