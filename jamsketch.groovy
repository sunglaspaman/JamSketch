import controlP5.ControlP5
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import jp.crestmuse.cmx.filewrappers.*
import jp.crestmuse.cmx.processing.*
import jp.crestmuse.cmx.inference.*

import jp.crestmuse.cmx.filewrappers.SCCDataSet
import jp.crestmuse.cmx.processing.gui.SimplePianoRoll
import jp.crestmuse.cmx.misc.*
import static jp.crestmuse.cmx.misc.ChordSymbol2.*
import java.io.File;
import controlP5.*;

class JamSketch extends SimplePianoRoll {

  GuideData guideData
  MelodyData2R melodyData
  boolean nowDrawing = false
  String username = ""
  int fullMeasure
  int mCurrentMeasure
  boolean restrictionIsFill = false
  boolean isCatching = false

  double nnOfMouseX 
  double catchedX

  PitchRestriction[] pitchRestriction=[null,null,null,null]
  int restrictionNum=0

  int mode=0
  
  static def CFG

  PrintWriter output
  static def outputFile
  int clickNum=0

  def prevElement 
  def nowElement 
  int pnn=-1
  int nnn
  int prevY
  int buttonX=80
  int buttonGap=buttonX/3
  int buttonNum=0
   
  int startX
  int endX

  ControlP5 p5ctrl;
  Button[] buttons;


  //String logFileName

  void setup() {
    super.setup()
    size(1200, 700)
    showMidiOutChooser()
    def p5ctrl = new ControlP5(this)
    p5ctrl.addButton("startMusic").
    setLabel("Start / Stop").setPosition(20+(buttonNum++*(buttonX+buttonGap)), 645).setSize(buttonX, 40)
    p5ctrl.addButton("resetMusic").
    setLabel("Reset").setPosition(20+(buttonNum++*(buttonX+buttonGap)), 645).setSize(buttonX, 40)
    p5ctrl.addButton("loadCurve").
    setLabel("Load").setPosition(20+(buttonNum++*(buttonX+buttonGap)), 645).setSize(buttonX, 40)
    p5ctrl.addButton("drawing").
    setLabel("drawing").setPosition(20+(buttonNum++*(buttonX+buttonGap)), 645).setSize(buttonX, 40)
    p5ctrl.addButton("edit").
    setLabel("edit").setPosition(20+(buttonNum++*(buttonX+buttonGap)), 645).setSize(buttonX, 40)
    p5ctrl.addButton("fillR").
    setLabel("fillR").setPosition(20+(buttonNum++*(buttonX+buttonGap)), 645).setSize(buttonX, 40)
    p5ctrl.addButton("restart").
    setLabel("restart").setPosition(20+(buttonNum++*(buttonX+buttonGap)), 645).setSize(buttonX, 40)

    buttons = new Button[4];
    buttons[0]=p5ctrl.addButton("strong").
    setLabel("strong").setPosition(20+(buttonNum++*(buttonX+buttonGap)), 645).setSize(buttonX, 40).onClick(event -> changeRestrictionList(0));
    buttons[1]=p5ctrl.addButton("medium").
    setLabel("medium").setPosition(20+(buttonNum++*(buttonX+buttonGap)), 645).setSize(buttonX, 40).onClick(event -> changeRestrictionList(1));
    buttons[2]=p5ctrl.addButton("weak").
    setLabel("weak").setPosition(20+(buttonNum++*(buttonX+buttonGap)), 645).setSize(buttonX, 40).onClick(event -> changeRestrictionList(2));
    buttons[3]=p5ctrl.addButton("none").
    setLabel("none").setPosition(20+(buttonNum++*(buttonX+buttonGap)), 645).setSize(buttonX, 40).onClick(event -> changeRestrictionList(3));

    buttons[restrictionNum].onClick(event -> changeRestrictionList(restrictionNum));



    if (CFG.MOTION_CONTROLLER != null) {
      CFG.MOTION_CONTROLLER.each { mCtrl ->
        JamSketch.main("JamSketchSlave", [mCtrl] as String[])
      }
    }

    initData()
    // add WindowListener (windowClosing) which calls exit();
    makeLogFile()

  }


  void initData() {

    for(int i=0;i<4;i++){
      pitchRestriction[i] = new PitchRestriction()
      pitchRestriction[i].setData(CFG)
    }
    pitchRestriction[0].setRestrictionListStrong()
    pitchRestriction[1].setRestrictionListMedium()
    pitchRestriction[2].setRestrictionListWeak()
    pitchRestriction[3].setRestrictionListNone()


    melodyData = new MelodyData2R(CFG.MIDFILENAME, width, this, this, CFG, pitchRestriction)
    smfread(melodyData.scc.getMIDISequence())
    def part =
      melodyData.scc.getFirstPartWithChannel(CFG.CHANNEL_ACC)
    setDataModel(
      part.getPianoRollDataModel(
	    CFG.INITIAL_BLANK_MEASURES,
            CFG.INITIAL_BLANK_MEASURES + CFG.NUM_OF_MEASURES
      ))
    if (CFG.SHOW_GUIDE)
      guideData = new GuideData(CFG.MIDFILENAME, width - 100, this)
    fullMeasure = dataModel.getMeasureNum() * CFG.REPEAT_TIMES;


    //melodyData.restrictionList=pitchRestriction.restrictionList
  }

  void draw() {
    super.draw()   
        //MusicElement e=melodyData.engine.mr.getMusicElement(melodyData.engine.OUTLINE_LAYER,1,1)
    //print(e.getMostLikely())
    if(restrictionIsFill){
      fillRestriction() 
      // int i=0
      // for(List<Integer> rList : pitchRestriction.restrictionList){
      //   print(i+" ")
      //   i++
      //   for (int note  : rList) {
      //       print(note+"_")
      //   }
      //   println("↻")
      // }
      // i=0
    }
    if (guideData != null)
      drawGuideCurve()
    if (CFG.FORCED_PROGRESS) {
      mouseX = beat2x(getCurrentMeasure() + CFG.HOW_IN_ADVANCE, getCurrentBeat());
    }
    if(pmouseX < mouseX &&
            mouseX > beat2x(getCurrentMeasure(), getCurrentBeat()) + 10) {
              //println(x2measure(mouseX)+"_"+x2tick(mouseX)+"_"+y2notenum(mouseY))
              
      if (isUpdatable()) {
        if(mode==0){
        
          storeCursorPosition()
          updateCurve()
          //println(notenum2y(y2notenum(mouseY))+"+"+mouseY)

        }
        
        }
      }

      if(mode==1){
          //println("111111111111111111")

          if(isInside(mouseX, mouseY)){
            
            nnOfMouseX = melodyData.engine.getNotenum(x2measure(mouseX),calcTick(x2beat(mouseX)))

            //output.println()
            
          }
          

          //println(x2measure(mouseX)+"_"+calcTick(x2beat(mouseX))+" "+mouseX+"_"+y2notenum(mouseY))
          if(nowDrawing){
            
            // print(y2notenum(mouseY))
            //print("||||")
            // print(nnOfMouseX)sas
            // print("______")
            // println(y2notenum(pmouseY) as int % 12  ==nnOfMouseX as int)
            if(isCatching){

            // melodyData.engine.change(x2measure(mouseX),x2tick(mouseX),y2notenum(mouseY))
              melodyData.engine.change(x2measure(catchedX),calcTick(x2beat(catchedX)),y2notenum(mouseY))

            }
          }

    }

    // if(mode==1){
    //   if(mousePressed){
    //     if(isCatching)
    //   fill(255,165,0)
    //   def eSt=melodyData.engine.getStartElement(x2measure(mouseX),calcTick(x2beat(mouseX)))
    //   double catchedMEsX1=beat2x(eSt.getMeasureNum() as double,eSt.tick() as double)
    //   def eEd=melodyData.engine.getEndElement(x2measure(mouseX),calcTick(x2beat(mouseX)))
    //   double catchedMEsX2=beat2x(eEd.getMeasureNum() as double,eEd.tick() as double)
      
    //   rect(catchedMEsX1,notenum2y(y2notenum(mouseY)),catchedMEsX2-catchedMEsX1,-17.5)
    //   }
    // }

    drawCurve()
    if (getCurrentMeasure() == CFG.NUM_OF_MEASURES - CFG.NUM_OF_RESET_AHEAD)
      //processLastMeasure()
      setTickPosition(0)

    melodyData.engine.setFirstMeasure(getDataModel().
      getFirstMeasure())
    enhanceCursor()
    drawProgress()
  }

  void drawCurve() {
    strokeWeight(3)
    stroke(0, 0, 255)
    (0..<(melodyData.curve1.size()-1)).each { i ->
      if (melodyData.curve1[i] != null &&
          melodyData.curve1[i+1] != null) {
        line(i, melodyData.curve1[i] as int, i+1,
             melodyData.curve1[i+1] as int)
             //println(i+" "+ melodyData.curve1[i] as int)
      }
    }    
  }

  void drawGuideCurve() {
    def xFrom = 100
    strokeWeight(3)
    stroke(100, 200, 200)
    (0..<(guideData.curveGuideView.size()-1)).each { i ->
      if (guideData.curveGuideView[i] != null &&
      guideData.curveGuideView[i+1] != null) {
        line(i+xFrom, guideData.curveGuideView[i] as int,
             i+1+xFrom, guideData.curveGuideView[i+1] as int)
      }
    }
  }

  void updateCurve() {
    melodyData.updateCurve(pmouseX, mouseX)
  }

  void storeCursorPosition() {
    (pmouseX..mouseX).each { i ->
      melodyData.curve1[i] = mouseY
    }
  }

  boolean isUpdatable() {
    if ((!CFG.ON_DRAG_ONLY || nowDrawing) &&
            isInside(mouseX, mouseY)) {
      int m1 = x2measure(mouseX)
      int m0 = x2measure(pmouseX)
      0 <= m0 && pmouseX < mouseX
    } else {
      false
    }
  }

  void processLastMeasure() {
    makeLog("melody")
    if (CFG.MELODY_RESETTING) {
      if (mCurrentMeasure < (fullMeasure - CFG.NUM_OF_RESET_AHEAD)) getDataModel().shiftMeasure(CFG.NUM_OF_MEASURES)
      melodyData.resetCurve()
      if (guideData != null) guideData.shiftCurve()
    }
  }

  void enhanceCursor() {
    if (CFG.CURSOR_ENHANCED) {
      fill(255, 0, 0)
      ellipse(mouseX, mouseY, 10, 10)
    }
  }

  void drawProgress() {
    if (isNowPlaying()) {
      def dataModel = getDataModel()
      mCurrentMeasure = getCurrentMeasure() +
              dataModel.getFirstMeasure() -
              CFG.INITIAL_BLANK_MEASURES + 1
      int mtotal = dataModel.getMeasureNum() *
                   CFG.REPEAT_TIMES
      textSize(32)
      fill(0, 255, 0)
      text(mCurrentMeasure + " / " + mtotal, 460, 675)
    }
  }
  
  void stop() {
    super.stop()
    //featext.stop()
  }

  void startMusic() {
    if (isNowPlaying()) {
      stopMusic()
      makeLog("stop")
      output.println("press stop")
      output.flush()
    } else {
      playMusic()
      makeLog("play")
      output.println("press play")
      output.flush()
    }
  }

  void resetMusic() {
    initData()
    setTickPosition(0)
    dataModel.setFirstMeasure(CFG.INITIAL_BLANK_MEASURES)
    makeLog("reset")
    output.println("press reset")
    output.flush()
  }

  @Override
  void musicStopped() {
    super.musicStopped()
//    if (microsecondPosition >= sequencer.getMicrosecondLength())
//      resetMusic()
  }

  void makeLog(action) {
    def logname = "output_" + (new Date()).toString().replace(" ", "_").replace(":", "-")
    if (action == "melody") {
      def midname = "${CFG.LOG_DIR}/${logname}_melody.mid"
      melodyData.scc.toWrapper().toMIDIXML().writefileAsSMF(midname)
      println("saved as ${midname}")
      def sccname = "${CFG.LOG_DIR}/${logname}_melody.sccxml"
      melodyData.scc.toWrapper().writefile(sccname)
      println("saved as ${sccname}")
      def jsonname = "${CFG.LOG_DIR}/${logname}_curve.json"
      saveStrings(jsonname, [JsonOutput.toJson(melodyData.curve1)] as String[])
      println("saved as ${jsonname}")
      def pngname = "${CFG.LOG_DIR}/${logname}_screenshot.png"
      save(pngname)
      println("saved as ${pngname}")

      // for debug
      new File("${CFG.LOG_DIR}/${logname}_noteList.txt").text = (melodyData.scc as SCCDataSet).getFirstPartWithChannel(1).getNoteList().toString()
//      new File("${CFG.LOG_DIR}/${logname}_noteOnlyList.txt").text = (melodyData.scc as SCCDataSet).getFirstPartWithChannel(1).getNoteOnlyList().toString()

    } else {
      def txtname = "${CFG.LOG_DIR}/${logname}_${action}.txt"
      saveStrings(txtname, [action] as String[])
      println("saved as ${txtname}")
    }
  }

  void loadCurve() {
    selectInput("Select a file to process:", "loadFileSelected")
    output.println("press load")
    output.flush()
  }

  void loadFileSelected(File selection) {
    if (selection == null) {
      println("Window was closed or the user hit cancel.")
    } else {
      def absolutePath = selection.getAbsolutePath()
      println("User selected " + absolutePath)
      if (absolutePath.endsWith(".json")) {
        def json = new JsonSlurper()
        melodyData.curve1 = json.parseText(selection.text)
        melodyData.updateCurve(0, width)
      } else if (selection.getCanonicalPath().endsWith(".txt")) {
        println("Reading ${absolutePath}")
        def table = loadTable(absolutePath, "csv")
        melodyData.curve1 = [null] * width
        int n = table.getRowCount()
        int m = melodyData.curve1.size() - 100
        for (int i in 100..<(melodyData.curve1.size() - 1)) {
          int from = (i - 100) * n / m
          int thru = ((i + 1) - 100) * n / m - 1
          melodyData.curve1[i] =
                  (from..thru).collect { notenum2y(table.getFloat(it, 0)) }.sum() /
                          (from..thru).size()
        }
        melodyData.updateCurve(0, width)
      }else {
        println("File is not supported")
        return
      }
    }
  }

  void mousePressed() {
    output.println("click:"+clickNum++)
    nowDrawing = true
    if(isInside(mouseX,mouseY)){
      nnOfMouseX = melodyData.engine.getNotenum(x2measure(mouseX),calcTick(x2beat(mouseX)))

      
    
      if(y2notenum(pmouseY) as int % 12 ==nnOfMouseX as int){
        isCatching=true
        catchedX=mouseX
      // catchingMeasure=x2measure(mouseX)
      // catchTick=calcTick(x2beat(mouseX))
      }
    writeOperationLog(1)
    output.flush();
    }


  }
  
  void mouseReleased() {
    nowDrawing = false
    isCatching= false
    
    if (isInside(mouseX, mouseY)) {
      //println(x2measure(mouseX))
      //println(CFG.NUM_OF_MEASURES)
      if (!melodyData.engine.automaticUpdate()) {
        melodyData.engine.outlineUpdated(
	   x2measure(mouseX) % CFG.NUM_OF_MEASURES,
           CFG.DIVISION - 1)
      }
      writeOperationLog(3)
      output.flush();
    }
    

  }

  void mouseDragged() {
    if(isInside(mouseX, mouseY)){
      writeOperationLog(2)
    }
  }

  void keyReleased() {
    if (key == ' ') {
      if (isNowPlaying()) {
      	stopMusic()
      } else {
        setTickPosition(0)
        getDataModel().setFirstMeasure(CFG.INITIAL_BLANK_MEASURES)
        playMusic()
      }
    } else if (key == 'b') {
      setNoteVisible(!isNoteVisible());
      println("Visible=${isVisible()}")
//    } else if (key == 'u') {
//      melodyData.updateCurve('all')
    }else if(keyCode == CONTROL){
      mode=0
      //print(mode)
    } else if (key == 'r') {
      restart()
    }

  }

  void keyPressed(){
    //int i=0
    if(keyCode == CONTROL){
      mode=1
      //print("mode is "+mode)
    }
  }

  public void exit() {
    println("exit() called.")
    output.close();
    super.exit()
    if (CFG.MOTION_CONTROLLER.any{mCtrl == "RfcommServer"}) RfcommServer.close()
  }

  static void main(String[] args) {
    JamSketch.CFG = evaluate(new File("./config.txt"))
    JamSketch.start("JamSketch")
  }

  void drawing(){
    mode=0
    output.println("press drawing")
    output.flush()
  }
  void edit(){
    mode=1
    output.println("press edit")
    output.flush()
  }
  void fillR(){
    restrictionIsFill = ! restrictionIsFill
  }

  void restart(){
    setTickPosition(0)
    output.println("press restart")
    output.flush()
  }

  int calcTick(double beat){
    double tick=beat
    tick*=CFG.DIVISION
    tick/=CFG.BEATS_PER_MEASURE
    int inttick=tick as int 
    inttick
  }

  int getNearest(List<Integer> list,int v){
    int i;		// ループ用
		int num;	// 配列の添え字
		int minv;	// 配列値-指定値vの絶対値

		// 配列の個数が1未満の処理
		//if ( 1 >list.length ) return -1;

		// 指定値と全ての配列値の差を比較
		num = 0;
		minv = Math.abs( list.get(0) - v );
		for ( i = 0; i < list.size();  i ++) {
			if ( Math.abs( list.get(i) - v ) < minv) {
				num = i;
				minv = Math.abs( list.get(i) - v );
			}
		}

		return list.get(num);
  }

  void editMelody(){}
  //

  void fillRestriction(){
    PitchRestriction pr = pitchRestriction[restrictionNum]
    int inCenterOfPR=48
    //println(y2notenum(height))
   //println("____________")
    for(int i=0;i<12;i++){
      for(int t=0;t<12;t++){
        for (int m : pr.tick2restrictionList(t).get(i)) {
        // print(c_note)
        // print(c_note.number())
        // print("=|")
        // print(notenum2y(48))
        // print("-")
        // print(notenum2y(47))
        // print("|=")
        // print("=|")
        // print(beat2x(i,0))
        // print("-")
        // print(beat2x(i+1,0))
        // print("|=")
          fill(255,0,0,127)
          rect(beat2x(i,0)+7.6*t,notenum2y(m as double +48),7.6,17.5)
          rect(beat2x(i,0)+7.6*t,notenum2y(m as double +60),7.6,17.5)
          rect(beat2x(i,0)+7.6*t,notenum2y(m as double +72),7.6,17.5)
        
        }
      }
      //println()
    }
    output.println("press fill")
    output.flush()
   }

   void changeRestrictionList(int restrictionNum){
      this.restrictionNum=restrictionNum
      //println("Now Restriction = "+this.restrictionNum)
      melodyData.engine.restrictionNum=restrictionNum
      //println("Now Restriction = "+melodyData.engine.restrictionNum)
      melodyData.engine.mr.name2layer.get(melodyData.engine.OUTLINE_LAYER).calculators.get(0).restrictionNum=restrictionNum
      //println("Now Restriction = "+melodyData.engine.mr.name2layer.get(melodyData.engine.OUTLINE_LAYER).calculators.get(0).restrictionNum)
      for (int i = 0; i < buttons.length; i++) {
        buttons[i].setColorBackground(color(0, 45, 90)); // グレーに戻す
      }
      buttons[restrictionNum].setColorBackground(color(0, 145, 0)); // 緑に設定
      //output.println("click:"+clickNum++)
      output.println("restriction changed to:"+restrictionNum);
      output.flush()

    }

    void head(){
                for(int m=0;m<12;m++){
            for(int t=0;t<12;t++){
              MusicElement e=melodyData.engine.mr.getMusicElement(melodyData.engine.MELODY_LAYER, m, t)
              if(pitchRestriction[restrictionNum].tick2restrictionList(t).get(m%12).contains(e.getMostLikely()))
                //e.setTiedFromPrevious(false)
                if(e.prev()!=null){
                  e.prev().setTiedFromPrevious(false)
                }
                // if(restrictionNum==0){
                //   e.next().setTiedFromPrevious(false)
                // }
                e.setEvidence(getNearest(pitchRestriction[restrictionNum].tick2restrictionList(t).get(m),e.getMostLikely()))

            }
          }
    }

   String getCurrent(){
    String current=""
    current+=year()
    current+=nf(month(),2)
    current+=nf(day(),2)
    current+=nf(hour(),2)
    current+=nf(minute(),2)
    current+=nf(second(),2)
   }

   void makeLogFile(){
    String logFileName="logfile"+getCurrent()+".txt"
    println(logFileName)
    String folderPath = sketchPath("logfiles"); // ディレクトリのパスを指定
    File folder = new File(folderPath);
  
  // フォルダが存在しない場合は作成
    if (!folder.exists()) {
      folder.mkdirs();
    }
    String filePath = folderPath +"/"+ logFileName; // ファイル名を指定
    output = createWriter(filePath);
    //output.println("click = ");
    //outputFile = evaluate(output)
   }

   void writeOperationLog(int mouseType){

    //= mr.getMusicElement(MELODY_LAYER, measure, tick)
    switch(mouseType){
      case 1:
        //mouseClicked
        
        if(mode==0){
          startX=mouseX
          output.println("mode:"+mode)
          output.println("start:("+"x:"+mouseX+","+"y:"+mouseY+")");
        } else {
        //output.println("click:"+clickNum++)

        //prevElement=melodyData.engine.mr.getMusicElement(melodyData.engine.MELODY_LAYER, x2measure(mouseX),calcTick(x2beat(mouseX)))
        //output.println("start:");
        //outputFile.click++
        //println(outputFile.click)
        if(isCatching){
          output.println("mode:"+mode)
          MusicElement e
          MusicElement starte
          MusicElement ende
          e=melodyData.engine.mr.getMusicElement(melodyData.engine.MELODY_LAYER, x2measure(mouseX), calcTick(x2beat(mouseX)))
          starte=melodyData.engine.getStartElement(x2measure(mouseX), calcTick(x2beat(mouseX)))
          ende=melodyData.engine.getEndElement(x2measure(mouseX), calcTick(x2beat(mouseX)))
          output.print("catch>")
          output.print("measure:"+starte.measure()+"→"+ende.measure())
          output.print("_tick:"+starte.tick()+"→"+ende.tick())
          output.print("_nn:"+y2notenum(mouseY)+"→")
        }
        }
        break;
      case 2:
        //mouseDragged
        //nowElement = melodyData.engine.mr.getMusicElement(melodyData.engine.MELODY_LAYER, x2measure(mouseX),calcTick(x2beat(mouseX)))
        //nowElement = melodyData.engine.getStartElement(int measure,int tick)
        // if(prevElement!=nowElement){
        //   prevElement=nowElement
        //   nnn=prevElement.getMostLikely()  
        // }
        // if(pnn!=nnn){
        //   pnn=nnn
        //   output.print("notenumber:"+pnn)
        //   output.println(" ("+"x:"+mouseX+","+"y:"+mouseY+")")
        // }
        
        break;
      case 3:
        if(mode==0){
          endX=mouseX

          int sM=x2measure(startX)
          int sT=calcTick(startX)%12
          int eM=x2measure(endX)
          int eT=calcTick(endX)
          //println(sM+"+|"+sT+"+|"+eM+"+|"+eT)
          for(int mea=sM;mea<=eM;mea++){
            for(int tc=0;tc<12;tc++){
              MusicElement e=melodyData.engine.mr.getMusicElement(melodyData.engine.MELODY_LAYER, mea, tc)
              //println(e.measure()+" "+e.tick()+" "+e.rest())
              MusicElement ec=melodyData.engine.mr.getMusicElement(melodyData.engine.OUTLINE_LAYER, mea, tc)
              if(!Double.isNaN(ec.getMostLikely())){
              output.println("measure:"+mea+"_tick:"+tc+"_nn:"+e.getMostLikely())
              //println("bbb"+e.getMostLikely())
              } else {
                output.println("measure:"+mea+"_tick:"+tc+"_nn:"+"rest")
              }
            }
          }
        output.println("end:("+"x:"+mouseX+","+"y:"+mouseY+")")
        output.println()
        } else {
          output.println(y2notenum(mouseY))
          output.flush()
        }

        break;
    }
   }



}
JamSketch.CFG = evaluate(new File("./config.txt"))
//JamSketch.CFG = evaluate(new File("./config_guided.txt"))
JamSketch.start("JamSketch")
// JamSketch.main("JamSketch", ["--external"] as String[])
  
