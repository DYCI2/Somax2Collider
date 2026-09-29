Influencer_gui {

	var <>win, style, style2, style3, input_node, output_node, onset_node, blinkbutton, keyboard, multiSlider_chroma, multiSlider_mfcc, levelmeter, osc_meter, sliderIn, sliderOut, textoutputlev, textinputlev, <>dbspec, input_bus = 0, soundFile, <>playButton, <>loopButton, <>select_input, <>input_bus_num, play_state, sf_path;
	var <>enableButton, <>onsetThresholdSlider, <>onsetLimiterSlider, <>pitchQualitySlider, <>onsetTypeMenu;
	*new {|influencer, input_lev, onset_threshold, audio_descriptors_onset, nodes, server, audio_influencer_info, parentClass|
		^super.newCopyArgs(influencer, ).init_Influencer(influencer, input_lev, onset_threshold = 0.1, audio_descriptors_onset, nodes, server, audio_influencer_info, parentClass);
	}

	init_Influencer {|influencer, input_lev, onset_threshold, audio_descriptors_onset, nodes, server, audio_influencer_info, parentClass|
		style = GMStyle()
		.mainColor_(Color(0.75, 0, 0.333))
		.secondColor_(Color(0.5, 0, 0.111))

		.borderSize_(1)
		.borderColor_(Color.gray)
		.secondBorderSize_(1)
		.secondBorderColor_(Color.white)
		.thirdBorderSize_(1)
		.thirdBorderColor_(Color(0.13095238095238, 0.030282738095238, 0.030282738095238))

		.font_(Font.default.deepCopy.size_(24))
		.fontColor_(Color.black)
		.fontColorDisabled_(Color(0.25, 0.25, 0.25))

		.outlineSize_(3)
		.outlineColor_(Color(1, 1, 1))

		.backColor_(Color(0.13095238095238, 0.030282738095238, 0.030282738095238))
		.backgroundColor_(Color(0.13095238095238, 0.030282738095238, 0.030282738095238))

		.disabledColor_(Color(0.5, 0.5, 0.5))
		.selectedColor_(Color(0.75, 0, 0.333))

		.helpersColor_(Color(1, 1, 1, 0.25))

		.beatColor_(Color(0, 1, 1, 0.5))

		.valueFontColor_(Color(1, 1, 1))
		.highlightColor_(Color(1, 1, 1, 0.5));

		style2 = GMStyle()
		.mainColor_(Color.blue) //Color(0.75, 0, 0.333))
		.secondColor_(Color(0.5, 0, 0.111))

		.borderSize_(0)
		.borderColor_(Color.gray)
		.secondBorderSize_(0)
		.secondBorderColor_(Color.white)
		.thirdBorderSize_(-7)
		.thirdBorderColor_(Color(0.13095238095238, 0.030282738095238, 0.030282738095238))

		.font_(Font.default.deepCopy.size_(64))
		.fontColor_(Color.black)
		.fontColorDisabled_(Color(0.25, 0.25, 0.25))

		.outlineSize_(3)
		.outlineColor_(Color(1, 1, 1))

		.backColor_(Color(1, 0.71230158730159, 0, 0.79365079365079))
		.backgroundColor_(Color(1, 0.71230158730159, 0, 0.79365079365079))

		.disabledColor_(Color(0.5, 0.5, 0.5))
		.selectedColor_(Color(0.75, 0, 0.333))

		.helpersColor_(Color(1, 1, 1, 0.25))

		.beatColor_(Color(0, 1, 1, 0.5))

		.outlineColor_(Color.blue)

		.valueFontColor_(Color(1, 1, 1))
		.highlightColor_(Color(1, 1, 1, 0.5));

		style3 = GMStyle()
		.outlineSize_(0)
		.secondBorderSize_(0)
		.borderSize_(0)
		.thirdBorderSize_(0)
		.font_(Font.default.deepCopy.size_(64));

		keyboard = GMKeyboard().style_(style)
		.margins_(1)
		// .displayHighlights_(true)
		// .highlights_([1, 1, 1, 1, 1, 1, 1])
		.mode_(\chromatic)
		.keyNumber_(13)
		.blackKeyColor_(Color.black)
		.keyColor_(Color.white)
		.blinkColor_(Color.yellow)
		.outlineKeys_(false)
		.action_({ |index|
			index.postln;
		});


		dbspec = ControlSpec(0.ampdb, 1.4126.ampdb, \db, 0, 0, \dB);

		input_node = nodes[influencer.asSymbol];
		output_node = parentClass.audio_group_lev[influencer.asSymbol].nodeID;
		// input_node = parentClass.audio_group_lev[influencer.asSymbol].nodeID;
		onset_node = audio_descriptors_onset[influencer.asSymbol];
		if(audio_influencer_info[influencer.asSymbol][1].isNumber)
		{
			input_bus = audio_influencer_info[influencer.asSymbol][1];
		};
		"PLAY_STATE".postln;
		(parentClass.model_server[\params][\audio_influencer_play]).postln;
		(parentClass.model_server[\params][\audio_influencer_loop]).postln;
		"PLAY_STATE".postln;

		server.sendMsg(\n_set, output_node,  *[\amp, dbspec.map(-120)]); // Init audio output OFF (avoid feedback)

		/*		nodes.postln;
		input_node.postln;*/

		/*		"ERROR2".postln;
		influencer.postln;
		input_lev.postln;
		onset_threshold.postln;
		audio_descriptors_onset.postln;
		nodes.postln;
		server.postln;
		audio_influencer_info.postln;
		"ERROR2".postln;*/



		win = Window.new(influencer.asString, Rect(0, 150, 0, 300));
		win.view.deleteOnClose = false; // win not destroyed
		win.layout_(
			VLayout(
				UserView.new.background_(Color(0.262, 0.262, 0.262)).layout_(
					VLayout(
						HLayout(
							StaticText().maxHeight_(22).fixedWidth_(120).string_(influencer.asString).stringColor_(Color(0.837, 0.837, 0.837)),
							enableButton = Button().maxHeight_(22).fixedWidth_(120).font_(Font("Helvetica", 11)).states_([["Disable", Color.black, Color.gray], ["Enable", Color.black, Color.green]]).value_(1), nil
						),
						HLayout(
							StaticText().maxHeight_(22).fixedWidth_(70).string_("Audio_In").stringColor_(Color(0.837, 0.837, 0.837)),
							input_bus_num = NumberBox().fixedWidth_(30).fixedHeight_(22).background_(Color.grey).normalColor_(Color.white).value_(input_bus).action_({|val|
								server.sendMsg(\n_set, input_node,  *[\input, val.value]);
							}),
							select_input = PopUpMenu().maxHeight_(22).maxWidth_(100).items_(["Input", "Audio file"]).font_(Font("Helvetica", 11)).background_(Color.gray(0.6)).action_({|menu|
								if(menu.value == 0)
								{
									parentClass.audio_influencer(influencer.asSymbol, \audioIn, input_bus_num.value); // audio File
									audio_influencer_info[influencer.asSymbol][2] = \audioIn;
									this.waveform_color(0);
								}
								{
									parentClass.audio_influencer(influencer , \soundFile, sf_path, play_state, loopButton.state); // audio File
									audio_influencer_info[influencer.asSymbol][2] = \soundFile;
									this.waveform_color(1);
								}
								// menu.item.postln;
								// (Platform.userAppSupportDir ++ "/TSupport/Interfaces/"++menu.item++".scd").load;
							}),
							Button().maxHeight_(22).fixedWidth_(60).font_(Font("Helvetica", 11)).states_([["read", Color(1, 0.71230158730159, 0, 0.79365079365079), Color.grey]]).action_({|val|
								var sf;
								FileDialog({ |paths|
									sf_path = paths[0];
									postln("Selected sound:"++sf_path);
									sf = SoundFile.openRead(sf_path);
									soundFile.soundfile = sf;            // set soundfile
									{0.1.wait;{
										soundFile.readWithTask(0, sf.numFrames);     // read in the entire file.
										soundFile.refresh;                  // refresh to display the file.
									}.defer}.fork;
									parentClass.audio_influencer(influencer , \soundFile, sf_path, play_state, loopButton.state); // audio File
								});

							}), nil
						),
						soundFile = SoundFileView().fixedHeight_(50).gridOn_(false).waveColors_([Color(0, 0.50972222222222, 1)]).peakColor_(Color(0, 0.8896164021164, 1)).rmsColor_(Color.blue), //.value_(Array.fill(13, {0})).indexThumbSize_(2.0).gap_(4);
						HLayout(
							playButton = GMPlayButton().fixedHeight_(20).fixedWidth_(40).style_(style3).setPlaying(parentClass.model_server[\params][\audio_influencer_play][1]).mode_(\stop).action_({ |pressed|
								if(pressed)
								{
									parentClass.audio_influencer(influencer, \play);
									play_state = 1
								}
								{
									parentClass.audio_influencer(influencer, \stop);
									play_state = 0
								}
							}),
							loopButton = GMSwitchButton().maxHeight_(20).fixedWidth_(40).style_(style3).state_(parentClass.model_server[\params][\audio_influencer_loop][1]).states_([(string: "Loop", color: Color.red), (string: "Loop", color: Color.green)]).action_({ |index|
								case
								{ index == 0 } { parentClass.audio_influencer(influencer, \loop, 0); }
								{ index == 1 } { parentClass.audio_influencer(influencer, \loop, 1); }
							}),
							nil
						),
						HLayout(
							// StaticText().maxHeight_(22).fixedWidth_(0).string_(""),

							levelmeter = LevelIndicator().fixedHeight_(22).numTicks_(37).numMajorTicks_(5).warning_(0.88).critical_(0.98).drawsPeak_(true).style_(\led).stepWidth_(1),
						),
						HLayout(
							StaticText().maxHeight_(20).fixedWidth_(36).string_("In").align_(\right).stringColor_(Color(0.837, 0.837, 0.837)),
							sliderIn = Slider().maxHeight_(10).fixedWidth_(190).orientation_(\horizontal).thumbSize_(15).value_(dbspec.unmap(0)).action_({|val|
								server.sendMsg(\n_set, input_node,  *[\amp, dbspec.map(val.value)]);

								textinputlev.string_(dbspec.map(val.value).round(0.1)); // .asStringWithFrac(1)
							}),
							textinputlev = StaticText().maxHeight_(20).fixedWidth_(36).string_("0.0").align_(\right).stringColor_(Color(0.837, 0.837, 0.837))
						),
						HLayout(
							onsetTypeMenu = PopUpMenu()
							.fixedHeight_(20)
							.items_(["Onset", "PitchOnset"])
							.font_(Font("Helvetica", 11))
							.background_(Color.gray(0.6))
							.action_({ |menu|
								audio_influencer_info[influencer.asSymbol][5] = menu.item;
								audio_influencer_info[influencer.asSymbol].postln;
								menu.item.postln;
							}), nil
						),
						HLayout(
							StaticText().maxHeight_(22).fixedWidth_(120).string_("Onset threshold").font_(Font("Helvetica", 11)).align_(\right).stringColor_(Color(0.837, 0.837, 0.837)),
							onsetThresholdSlider = GMFaderSlider().maxHeight_(12).fixedWidth_(120).orientation_(\horizontal).min_(0).max_(2).style_(style2).value_(onset_threshold).action_({ |value|
								server.sendMsg(\n_set, onset_node,  *[\threshold, value]);
								onset_node.postln;
								value.postln;
							}), nil
						).spacing_(5),
						HLayout(
							StaticText().maxHeight_(22).fixedWidth_(120).string_("Onset Limiter (ms)").font_(Font("Helvetica", 11)).align_(\right).stringColor_(Color(0.837, 0.837, 0.837)),
							onsetLimiterSlider =  GMFaderSlider().maxHeight_(12).fixedWidth_(120).orientation_(\horizontal).min_(0).max_(500).style_(style2).value_(audio_influencer_info[influencer.asSymbol][0]).action_({ |value|
								audio_influencer_info[influencer.asSymbol][0] = value;
								audio_influencer_info[influencer.asSymbol].postln;
								value.postln;
							}), nil
						).spacing_(5),
						HLayout(
							StaticText().maxHeight_(22).fixedWidth_(120).string_("Pitch Quality").font_(Font("Helvetica", 11)).align_(\right).stringColor_(Color(0.837, 0.837, 0.837)),
							pitchQualitySlider = GMFaderSlider().maxHeight_(12).fixedWidth_(120).orientation_(\horizontal).min_(0.0).max_(1.0).style_(style2).value_(audio_influencer_info[influencer.asSymbol][4]).action_({ |value|
								audio_influencer_info[influencer.asSymbol][4] = value;
								audio_influencer_info[influencer.asSymbol].postln;
								// value.postln;
							}), nil
						).spacing_(5),
						HLayout(
							StaticText().maxHeight_(20).fixedWidth_(36).string_("Out").align_(\right).stringColor_(Color(0.837, 0.837, 0.837)),
							sliderOut = Slider().maxHeight_(10).fixedWidth_(190).orientation_(\horizontal).thumbSize_(15).value_(dbspec.unmap(-120).max(-120)).action_({|val|
								var dbVal;
								dbVal = dbspec.map(val.value).max(-120);

								server.sendMsg(\n_set, output_node,  *[\amp, dbVal]);

								textoutputlev.string_(dbVal.round(0.1)); // .asStringWithFrac(1)
							}),
							textoutputlev = StaticText().maxHeight_(20).fixedWidth_(36).string_("-120").align_(\right).stringColor_(Color(0.837, 0.837, 0.837))
						),
						HLayout(
							// Button().maxHeight_(22).fixedWidth_(22).font_(Font("Helvetica", 11)).states_([["", Color.black, Color.gray], ["", Color.black, Color.green]]),
							blinkbutton = GMRoundButton().style_(style).blinkColor_(Color.yellow).fixedWidth_(40).fixedHeight_(30),
							keyboard.minSize_(80@40), //	.margins_(0)
							// MultiSliderView().fixedWidth_(80).fixedHeight_(60)
							multiSlider_chroma = GMFaderMultiSlider().style_(style).backColor_(Color.black)
							// .outlineColor_(Color.blue)
							.polarity_(\uni)
							.max_(1)
							.displayHighlights_(false)
							.displayValues_(false)
							.minAlpha_(1)
							.values_([
								0, 0, 0, 0,
								0, 0, 0, 0,
								0, 0, 0, 0,
							]).slidersColors_([
								Color(1.0, 0.0, 0.0),     // Rouge
								Color(1.0, 0.5, 0.0),     // Orange
								Color(1.0, 0.8, 0.0),     // Jaune-orange
								Color(1.0, 1.0, 0.0),     // Jaune
								Color(0.5, 1.0, 0.0),     // Jaune-vert
								Color(0.0, 1.0, 0.0),     // Vert
								Color(0.0, 1.0, 0.7),     // Turquoise
								Color(0.0, 0.6, 1.0),     // Bleu ciel
								Color(0.0, 0.3, 1.0),     // Bleu
								Color(0.3, 0.0, 1.0),     // Indigo
								Color(0.6, 0.0, 1.0),     // Violet
								Color(1.0, 0.0, 1.0)      // Magenta
							]),
							multiSlider_mfcc = GMFaderMultiSlider().style_(style).backColor_(Color.black)
							// .outlineColor_(Color.blue)
							.polarity_(\bi)
							.max_(60)
							.scale_(\lin)

							// .values_(freqs)
							.centerValues_(false)
							.helperSubdivisions_(2)
							.displayHighlights_(false)
							.displayValues_(false)
							.minAlpha_(1)
							// .highlights_(highlights)
/*							.min_(60.0)
							.max_(60.0)
							.polarity_(\bi)
							.displayHighlights_(false)
							.displayValues_(false)
							.minAlpha_(1)*/
							.values_([
								0.0, 60.0, -60, -50,
								-30, -20, -10, 0,
								10, 20, 30, 40, 50, 60
							]).slidersColors_([
/*								Color(1.0, 0.0, 0.0),     // Rouge
								Color(1.0, 0.5, 0.0),     // Orange
								Color(1.0, 0.8, 0.0),     // Jaune-orange
								Color(1.0, 1.0, 0.0),     // Jaune
								Color(0.5, 1.0, 0.0),     // Jaune-vert
								Color(0.0, 1.0, 0.0),     // Vert
								Color(0.0, 1.0, 0.7),     // Turquoise
								Color(0.0, 0.6, 1.0),     // Bleu ciel
								Color(0.0, 0.3, 1.0),     // Bleu*/
								Color(0.3, 0.0, 1.0),     // Indigo
/*								Color(0.6, 0.0, 1.0),     // Violet
								Color(1.0, 0.0, 1.0)      // Magenta*/
							]),
						).spacing_(2)
						/*						HLayout(
						// Button().maxHeight_(22).fixedWidth_(22).font_(Font("Helvetica", 11)).states_([["", Color.black, Color.gray], ["", Color.black, Color.green]]),

						// LevelIndicator().maxHeight_(14).fixedWidth_(187).numTicks_(37).numMajorTicks_(5).warning_(0.88).critical_(0.98).drawsPeak_(true), nil
						),*/

					) // borde
				)
			)
		);
		win.front;
		// receive OSC para view-meter
		osc_meter = OSCFunc({|msg|
			if (msg[2] == input_node) {
				{
					levelmeter.peakLevel = msg[3].ampdb.linlin(-60, 0, 0, 1);
					levelmeter.value = msg[4].ampdb.linlin(-60, 0, 0, 1);
				}.defer;
			}
		}, '/meter');

		influencer.postln;
		// parentClass.model_server[\params][\audio_influencer_SF].postln;
		if((parentClass.model_server[\params][\audio_influencer_SF]).notNil && parentClass.model_server[\params][\audio_influencer_SF][1].notNil)
		{
			var sf;
			(parentClass.model_server[\params][\audio_influencer_SF]).postln;
			(parentClass.model_server[\params][\audio_influencer_SF][1]).postln;
			(parentClass.model_server[\params][\audio_influencer_SF][1]).class.postln;
			sf = SoundFile.openRead(parentClass.model_server[\params][\audio_influencer_SF][1]);
			soundFile.soundfile = sf;            // set soundfile
			{0.1.wait;{
				soundFile.readWithTask(0, sf.numFrames);     // read in the entire file.
				soundFile.refresh;                  // refresh to display the file.
			}.defer}.fork;
		};
		if((parentClass.model_server[\params][\audio_influencer_play][0]).notNil)
		{
			(parentClass.model_server[\params][\audio_influencer_play][1]).postln;
			playButton.setPlaying(parentClass.model_server[\params][\audio_influencer_play][1]);
			if(parentClass.model_server[\params][\audio_influencer_play][1] == true)
			{
				play_state = 1
			}
			{
				play_state = 0
			};
		};
		if((parentClass.model_server[\params][\audio_influencer_loop][0]).notNil)
		{
			(parentClass.model_server[\params][\audio_influencer_loop][1]).postln;
			loopButton.state_(parentClass.model_server[\params][\audio_influencer_loop][1])
		};
		if((parentClass.model_server[\params][\audio_influencer_select_input]).notNil)
		{
			// (parentClass.model_server[\params][\audio_influencer_select_input][1]).postln;
			select_input.valueAction_(parentClass.model_server[\params][\audio_influencer_select_input])
		};
	}
	pitch {|pitch|
		keyboard.playKey(pitch%12);
	}
	chroma {|chroma|
		/*	"blink".postln;
		pitch.postln;*/
		// blinkbutton.blink;
		// keyboard.playKey(pitch%12);
		{AppClock.sched(0, {multiSlider_chroma.values_(chroma)})}.fork;
	}
	mfcc {|mfcc|
		// mfcc.postln;
		{AppClock.sched(0, {multiSlider_mfcc.values_(mfcc)})}.fork;
	}
	blink {
		/*	"blink".postln;
		pitch.postln;*/
		blinkbutton.blink;
		// keyboard.playKey(pitch%12);
		// {AppClock.sched(0, {multiSlider_chroma.values_(chroma_array)})}.fork;
	}
	load_soundFile {|soundfilePath|
		var sf;
		soundfilePath.postln;
		sf_path = soundfilePath.asString;
		sf = SoundFile.openRead(sf_path); // for SoundFileView
		soundFile.soundfile = sf;            // set soundfile
		{0.1.wait;{
			soundFile.readWithTask(0, sf.numFrames);     // read in the entire file.
			soundFile.refresh;                  // refresh to display the file.
		}.defer}.fork;
	}
	waveform_color {|state|
		case
		{state == 0}
		{
			soundFile.waveColors_([Color.grey]).peakColor_(Color.grey).rmsColor_(Color.grey);
		}
		{state == 1}
		{
			soundFile.waveColors_([Color(0, 0.50972222222222, 1)]).peakColor_(Color(0, 0.8896164021164, 1)).rmsColor_(Color.blue);
		}
	}

}
/*a = GMStyle
a.borderSize_(0)
~keyboard.playKey(3)*/