Player_gui {

	var <>win, style, style2, <>onset_node, blinkbutton, keyboard, multiSlider, multiSlider_mfcc, levelmeter, osc_meter, osc_meter2,  <>slider, textoutputlev, <>dbspec, input_bus, <>influence_source, model, <>player_mvc_model_dico, model_params, <>corpus_items, <>corpus_menu, <>soundFile, <>menuButton, style, style2, style3, style4, style_rond, style_rond_solo, <>blinking, influ_onset_button, influ_chroma, influ_pitch, influ_mfcc, levelmeter0, levelmeter1, audioOut_synth_node, player_blinkbutton, <>state_slider, <>matchButton, <>timeoutButton, <>current_corpus, <>playing_mode_Button, <>enable_Button, <>weight_multislider, <>qualitySlider, <>probSlider, <>time_stretch_slider, <>selected_influ, <>preset_menu;

	var slider, dbText, levelIndic; // = Array.new;
	var <>outputMenu, numberBox;
	var containerView;
	var spacing = 9, sliderHeight = 100, indicatorHeight = 100, indicatorWidth = 7;
	var modes; // = ["mono", "stereo", "multichannel", "ambisonic", "aoo"];
	var indicatorCounts; // = [1, 2, 16, 24, 18];
	var updateIndicators, <>setupMode;
	var <>current_preset; // nom du preset actuellement chargé
	var <>currentVal, aooPanel, offset_out = 0, offset_out2 = 1, <>offset_out_mono = 0, <>offset_out_stereo0 = 0, <>offset_out_stereo1 = 1, <>offset_out_multichannel = 0, <>offset_out_ambisonic = 0, <>offset_out_aoo = 24, <>offset_out_mono_box, <>offset_out_stereo0_box, <>offset_out_stereo1_box, <>offset_out_multichannel_box, <>offset_out_ambisonic_box, <>offset_out_aoo_box, <>modePanel;
	var timeoutView, <>timeoutUpdateContent, <>timeoutVal = 2.0;
	var <>currentModeIndex = 1;
	var <>cutButton, <>sparseButton, <>timeoutToggle, <>continuitySlider,
	<>transpButtons, <>onsetThresholdSlider, <>onsetLimiterSlider,
	<>pitchQualitySlider, <>outgoingInfluenceMenu, <>timeStretchToggle,
	<>beatAlignButton;
	var <>transpStates;

	*new {|player, input_lev, onset_threshold, audio_descriptors_onset, nodes, server, parentClass|
		^super.newCopyArgs(player, parentClass).init_player(player, input_lev, onset_threshold = 0.1, audio_descriptors_onset, nodes, server, parentClass);
	}

	init_player {|player, input_lev, onset_threshold, audio_descriptors_onset, nodes, server, parentClass|

		style = GMStyle()
		.mainColor_(Color.blue) //Color(0.75, 0, 0.333))
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

		.outlineColor_(Color.blue)

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

		style3 = GMStyle().fontColor_(Color(0.837, 0.837, 0.837));


		style4 = GMStyle()
		.mainColor_(Color(0.36170634920635, 1, 0, 0.84920634920635))
		.secondColor_(Color(0.36170634920635, 1, 0, 0.84920634920635))

		.borderSize_(0)
		.borderColor_(Color.gray)
		.secondBorderSize_(0)
		.secondBorderColor_(Color.white)
		.thirdBorderSize_(-7)
		.thirdBorderColor_(Color(0.36170634920635, 1, 0, 0.84920634920635))

		.outlineSize_(3)
		.outlineColor_(Color(1, 1, 1))

		.backColor_(Color(0.36170634920635, 1, 0, 0.84920634920635))
		.backgroundColor_(Color(0.36170634920635, 1, 0, 0.84920634920635))

		.disabledColor_(Color(0.36170634920635, 1, 0, 0.84920634920635))
		.selectedColor_(Color(0.36170634920635, 1, 0, 0.84920634920635))

		.helpersColor_(Color(1, 1, 1, 0.25))

		.beatColor_(Color(0, 1, 1, 0.5))

		.outlineColor_(Color.blue)

		.valueFontColor_(Color(0.36170634920635, 1, 0, 0.84920634920635))
		.highlightColor_(Color(1, 1, 1, 0.5));

		style_rond = GMStyle()
		.mainColor_(Color(1, 0.71230158730159, 0, 0.79365079365079)) //Color(0.75, 0, 0.333))
		.borderSize_(0)
		.secondBorderSize_(0)
		.thirdBorderSize_(0)
		.outlineSize_(0)
		.backgroundColor_(Color(0.13095238095238, 0.030282738095238, 0.030282738095238))
		.outlineColor_(Color(0.837, 0.837, 0.837));

		style_rond_solo = GMStyle()
		.mainColor_(Color(1, 0.71230158730159, 0, 0.79365079365079)) //Color(0.75, 0, 0.333))
		.borderSize_(0)
		.secondBorderSize_(0)
		.thirdBorderSize_(0)
		.outlineSize_(0)
		.backgroundColor_(Color(1, 0.71230158730159, 0, 0.79365079365079))
		.outlineColor_(1, 0.7, 0, 0.8);


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
			// Synth(\GMKeyboard, [\freq, freqs[index]]);
		});

		/*	"parentClass".postln;
		parentClass.postln;
		parentClass.class.postln;
		parentClass.players.postln;
		parentClass.nodes.postln;
		"audio_influencer_dico".postln;
		parentClass.audio_influencer_dico.asArray.postln;
		"parentClass".postln;*/
		levelIndic = Array.new;
		modes = ["mono", "stereo", "multichannel", "ambisonic", "aoo"];
		indicatorCounts = [1, 2, 16, 24, 18];
		dbspec = ControlSpec(0.ampdb, 1.4126.ampdb, \db, 0, 0, \dB);


		////////MVC
		transpStates = List.fill(11, {0});
		transpButtons = List.new;

		player_mvc_model_dico = Dictionary.new;
		model_params = ();

		model = (
			params: model_params,
			setParam: { |self, name, value|
				self[\params][name] = value;
				self.changed(\param, name, value);
			},
		);

		player_mvc_model_dico.add(player.asSymbol -> model);

		////////MVC

		// Menu
		/*		corpus_items = parentClass.corpus_itmes.values.collect({|mess|
		mess[0];
		});
		corpus_items = corpus_items.sort.addFirst("refresh corpus");
		"corpuscorpus".postln;
		parentClass.corpus_itmes.postln;*/
		corpus_items = parentClass.menu_corpus_items;
		// onset_node = audio_descriptors_onset[player.asSymbol];
		/*		if (audio_descriptors_onset[player.asSymbol].notNil)
		{
		onset_node = audio_descriptors_onset[player.asSymbol];
		}
		{
		onset_node = -1; // uninitialised node
		};*/
		/*		"onset_node".postln;
		onset_node.postln;*/
		parentClass.audio_influencer_info.postln;
		// parentClass.audio_influencer_info[player.asSymbol][0].postln;
		onset_threshold.postln;
		// parentClass.audio_influencer_info[player.asSymbol][0]

		// Dynamic View
		timeoutView = View().minSize_(35@16);

		// Fonction update content
		timeoutUpdateContent = { |state|
			var newView;

			timeoutView.children.do(_.remove);
			timeoutView.layout = nil;

			newView = if (state == 1) {
				NumberBox().font_(Font("Helvetica", 11)).stringColor_(Color(0.837, 0.837, 0.837)).fixedWidth_(35).fixedHeight_(16).background_(Color.grey).normalColor_(Color.white).value_(timeoutVal).maxDecimals_(2).minDecimals_(2).action_({|val|

					parentClass.set_time_out(player.asSymbol, val.value.asFloat);
					timeoutVal = val.value;
					timeoutVal.postln;

				})
			} {
				parentClass.set_time_out(player.asSymbol, \set_timeout, \None); // timeout
				StaticText()
				.string_("endless")
				.stringColor_(Color(0.837, 0.837, 0.837))
				.font_(Font("Helvetica", 12))
				.align_(\left)
				.fixedWidth_(35)
				.fixedHeight_(16)
			};

			timeoutView.layout = HLayout(newView);
		};

		win = Window.new(player.asString, Rect(0, 50, 0, 10));
		win.view.deleteOnClose = false; // win not destroyed

		win.toFrontAction_({
			if(parentClass.menu_corpus_items.notNil && (parentClass.menu_corpus_items.size > 0)) {
				var savedVal = corpus_menu.value; // sauvegarder position avant defer
				defer {
					corpus_menu.items_(parentClass.menu_corpus_items);
					corpus_menu.value_(savedVal); // restaurer position
				};
			};
		});
		win.view.keyDownAction_({ |doc, char|
			if (char.asString == "m") {
				parentClass.somax_gui.wind.view.front;
			}
		});
		win.layout_(
			VLayout(
				View.new.background_(Color(0.262, 0.262, 0.262)).layout_(
					VLayout(
						HLayout(
							StaticText().maxHeight_(22).fixedWidth_(100).string_(player.asString).stringColor_(Color(0.837, 0.837, 0.837)),
							enable_Button = Button().maxHeight_(22).fixedWidth_(100).font_(Font("Helvetica", 11)).states_([["Disable", Color.black, Color.gray], ["Enable", Color.black, Color.green]]).value_(1).action_({|butt|
								if(butt.value == 0)
								{
									parentClass.set_params(player.asSymbol,  \enabled, 0);
									// model_params.put(\enabled, 'False'); //MVC
								}
								{
									parentClass.set_params(player.asSymbol,  \enabled, 1);
									// model_params.put(\enabled, 'True'); //MVC
								}

							}),
							preset_menu = PopUpMenu()
							.fixedHeight_(22)
							.fixedWidth_(100)
							.items_(["presets", "new", "replace"] ++ parentClass.list_presets)
							.font_(Font("Helvetica", 11))
							.background_(Color.gray(0.6))
							.allowsReselection_(true)
							.action_({ |menu|
								var item = menu.item.asString;
								var current;
								case
								{ menu.value == 0 } {
									// "presets" header, ignorer
								}
								{ item == "new" } {
									var nameWin, tf, okBtn;
									nameWin = Window("New Preset", Rect(200, 200, 250, 80)).front;
									tf = TextField(nameWin, Rect(10, 10, 230, 25))
									.string_("preset name");
									okBtn = Button(nameWin, Rect(80, 45, 90, 25))
									.states_([["Save", Color.black, Color.green]])
									.action_({
										var name = tf.string.stripWhiteSpace;
										if(name.size > 0) {
											parentClass.save_preset(name, player.asSymbol);
											current_preset = name; // ← mémoriser
											nameWin.close;
										};
									});
								}
								{ item == "replace" } {
									// Utiliser le preset actuellement chargé
									if(current_preset.notNil) {
										parentClass.save_preset(current_preset, player.asSymbol);
										("Preset replaced: " ++ current_preset).postln;
									} {
										// Fallback : premier preset de la liste
										current = menu.items.detect({ |it|
											var s = it.asString;
											(s != "presets") && (s != "new") && (s != "replace")
										});
										if(current.notNil) {
											parentClass.save_preset(current.asString, player.asSymbol);
											current_preset = current.asString; // ← mémoriser
										};
									};
								}
								{ true } {
									current_preset = item; // ← mémoriser le preset chargé
									parentClass.load_preset(item, player.asSymbol);
								};
							}),
						),
						HLayout(
							influence_source = ListView() /////////////////////////////////////influencer Synth/////////////////////////////////
							// .fixedWidth_(100)
							.fixedWidth_(120).fixedHeight_(45)
							//.items_(parentClass.players[player.asSymbol]) //["player1", "player2", "player3", "player4", "player5", "payer6"]) //influencer.asArray)
							.items_([])
							.background_(Color.black(0.6))
							.stringColor_(Color.green(1))
							.alpha_(1)
							.hiliteColor_(Color.red(0.6))
							.selectedStringColor_(Color.green(1))
							.selectionMode_(\extended)
							.canReceiveDragHandler_({true})
							.receiveDragHandler_({|drag|
								View.currentDrag.postln;
								View.currentDrag.do({|influitem| // loop selected influencer items
									// parentClass.audio_influencer_dico[influitem] = parentClass.audio_influencer_dico[influitem].add(player); // update dico
									parentClass.connection(influitem, player); // connect
								});
								parentClass.player_gui_dico[player].influence_source.items_(parentClass.players[player].drop(1).asArray); // add influencer to player gui
							})
							.selectionAction_({|sbs|
								selected_influ= influence_source.selection;
							})
							.keyDownAction_({ |doc, char|
								if (char.asString == "x") { // dicconnect agent
									influence_source.items[selected_influ].do({|influitem| // loop selected influencer items
										// audio_influencer_dico[influitem] = audio_influencer_dico[influitem].removeAll(agenitem); // update dico
										parentClass.disconnection(influitem, player); // dicconnect
									});
									parentClass.player_gui_dico[player].influence_source.items_(parentClass.players[player].drop(1).asArray); // remove influencer to player gui
								}
							}),
						),
						// SoundFileView().fixedHeight_(50), //.value_(Array.fill(13, {0})).indexThumbSize_(2.0).gap_(4);
						HLayout(
							// StaticText().maxHeight_(22).fixedWidth_(0).string_(""),
							influ_pitch = NumberBox().fixedWidth_(30).fixedHeight_(22).background_(Color.grey).normalColor_(Color.white),
							influ_onset_button = GMRoundButton().style_(style).blinkColor_(Color.yellow).fixedWidth_(22).fixedHeight_(22),
							influ_chroma = GMFaderMultiSlider().fixedWidth_(80).fixedHeight_(22).style_(style).backColor_(Color.black)
							// .outlineColor_(Color.blue)
							.displayHighlights_(false)
							.displayValues_(false)
							.minAlpha_(1)
							.values_([
								0, 0, 0, 0,
								0, 0, 0, 0,
								0, 0, 0, 0]).slidersColors_([
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
							influ_mfcc = GMFaderMultiSlider().fixedWidth_(80).fixedHeight_(22).style_(style).backColor_(Color.black)
							.polarity_(\bi)
							.max_(60)
							.scale_(\lin)
							.centerValues_(false)
							.displayHighlights_(false)
							.displayValues_(false)
							.minAlpha_(1)
							.values_([
								0.0, 60.0, -60, -50,
								-30, -20, -10, 0,
								10, 20, 30, 40, 50, 60
							]).slidersColors_([
								Color(0.3, 0.0, 1.0),     // Indigo
							]),
							nil
						),
					)
				),
				View.new.background_(Color(0.262, 0.262, 0.262)).layout_(
					HLayout(
						StaticText().font_(Font("Helvetica", 11)).maxHeight_(22).fixedWidth_(150).string_("Corpus Recording").align_(\center).stringColor_(Color(0.837, 0.837, 0.837))
					).spacing_(0),
				),
				View.new.background_(Color(0.262, 0.262, 0.262)).layout_(
					VLayout(
						HLayout(nil,
							StaticText().font_(Font("Helvetica", 11)).maxHeight_(22).fixedWidth_(150).string_("Player Controls").align_(\center).stringColor_(Color(0.837, 0.837, 0.837)), nil
						),
						HLayout(
							corpus_menu = PopUpMenu().fixedHeight_(22).fixedWidth_(220).items_(corpus_items).font_(Font("Helvetica", 11)).background_(Color.gray(0.6)).allowsReselection_(true).action_({|menu|
								parentClass.load_corpus(player , menu.item.asString.drop(4));
								current_corpus = menu.item.asString.drop(4);
							}),
							menuButton = GMRoundButton().style_(style).blinkColor_(Color.yellow).fixedWidth_(20).fixedHeight_(20), nil
						),
						HLayout(
							View().fixedSize_(138@200).background_(Color.white).layout_(VLayout( // for rect margins
								View().background_(Color(0.262, 0.262, 0.262)).layout_(
									VLayout(
										VLayout(
											HLayout(
												StaticText().font_(Font("Helvetica", 12)).maxHeight_(10).fixedWidth_(86).
												string_("            Balance").align_(\left).stringColor_(Color(0.837, 0.837, 0.837)), nil
											),
											HLayout(
												StaticText().font_(Font("Helvetica", 11)).maxHeight_(10).fixedWidth_(80).string_("    Internal").stringColor_(Color(0.837, 0.837, 0.837)),
												StaticText().font_(Font("Helvetica", 11)).maxHeight_(10).fixedWidth_(80).string_("External").stringColor_(Color(0.837, 0.837, 0.837)),	 nil
											).margins_([0, 0, 0, 0]),
											HLayout(
												StaticText().font_(Font("Helvetica", 8)).maxHeight_(10).fixedWidth_(200).string_("  mel  harm  mfcc    mel  harm  mfcc").stringColor_(Color(0.837, 0.837, 0.837)), nil
											).margins_([0, 0, 0, 0]),
										).spacing_(3),
										HLayout(
											weight_multislider = GMFaderMultiSlider().style_(style).backColor_(Color.black).fixedHeight_(75).fixedWidth_(130).values_([0.25, 0.25, 0.25, 1, 0.5, 0.5]).action_({ |value| parentClass.set_params(player, \weights, value)}).slidersColors_([
												Color(1.0, 0.0, 0.0),
												Color(0.0, 0.3, 1.0),
												Color(0.0, 1.0, 0.0),
											]), nil,
											// LevelIndicator().maxHeight_(14).fixedWidth_(187).numTicks_(37).numMajorTicks_(5).warning_(0.88).critical_(0.98).drawsPeak_(true), nil
										).margins_([0, 0, 0, 0]),
										VLayout(
											HLayout(
												GMFaderSlider().fixedHeight_(10).fixedWidth_(17).orientation_(\horizontal).min_(0).max_(10).style_(style2).value_(2).roundValue_(1).action_({|val|
													parentClass.ngram_size(player.asSymbol, \self, val.value);
												}),
												GMFaderSlider().fixedHeight_(10).fixedWidth_(17).orientation_(\horizontal).min_(0).max_(10).style_(style2).value_(2).roundValue_(1).action_({|val|
													parentClass.ngram_size(player.asSymbol, \selfharmonic, val.value);
												}),
												GMFaderSlider().fixedHeight_(10).fixedWidth_(17).orientation_(\horizontal).min_(0).max_(10).style_(style2).value_(2).roundValue_(1).action_({|val|
													parentClass.ngram_size(player.asSymbol, \selfmfcc, val.value);
												}),
												GMFaderSlider().fixedHeight_(10).fixedWidth_(17).orientation_(\horizontal).min_(0).max_(10).style_(style2).value_(2).roundValue_(1).action_({|val|
													parentClass.ngram_size(player.asSymbol, \melodic, val.value);
												}),
												GMFaderSlider().fixedHeight_(10).fixedWidth_(17).orientation_(\horizontal).min_(0).max_(10).style_(style2).value_(2).roundValue_(1).action_({|val|
													parentClass.ngram_size(player.asSymbol, \harmonic, val.value);
												}),
												GMFaderSlider().fixedHeight_(10).fixedWidth_(17).orientation_(\horizontal).min_(0).max_(10).style_(style2).value_(2).roundValue_(1).action_({|val|
													parentClass.ngram_size(player.asSymbol, \mfcc, val.value);
												}),
												nil
											).margins_([4, 3, 0, 2]).spacing_(4),
											HLayout(
												StaticText().font_(Font("Helvetica", 11)).maxHeight_(22).fixedWidth_(120).string_("Memory length").stringColor_(Color(0.837, 0.837, 0.837)), nil
											).margins_([4, 2, 0, 2]),
											HLayout(
												StaticText().font_(Font("Helvetica", 11)).maxHeight_(22).fixedWidth_(120).string_("Pitch match").stringColor_(Color(0.837, 0.837, 0.837)), nil
											).margins_([2, 4, 0, 2]),
											HLayout(
												Button().font_(Font("Helvetica", 11)).fixedWidth_(50).fixedHeight_(19).states_([["mod12", Color(0.38, 0.36, 0.36), Color.gray],["mod12", Color.green, Color.grey]]).value_(0).action_({|butt|
													parentClass.set_classifier_self(player.asSymbol,  butt.value);
												}),
												Button().font_(Font("Helvetica", 11)).fixedWidth_(50).fixedHeight_(19).states_([["mod12", Color(0.38, 0.36, 0.36), Color.grey],["mod12", Color.red, Color.grey]]).value_(0).action_({|butt|
													parentClass.set_classifier_melodic(player.asSymbol,  butt.value);
												}), nil
											).spacing_(3),
										),
									).spacing_(0).margins_([2, 2, 2, 2]),
								)
							).margins_(1) // rect margins
							),
							View().background_(Color.white).layout_(VLayout( // for rect margins
								View().background_(Color(0.262, 0.262, 0.262)).layout_(
									VLayout(
										HLayout(
											StaticText().font_(Font("Helvetica", 11)).maxHeight_(20).fixedWidth_(40).string_("Timeout").stringColor_(Color(0.837, 0.837, 0.837)),
											timeoutView,
											timeoutToggle = Button().font_(Font("Helvetica", 17)).fixedWidth_(16).fixedHeight_(16)
											.states_([["x", Color.grey, Color(0.262, 0.262, 0.262)],
												["x", Color(1, 0.7, 0, 0.8), Color(0.262, 0.262, 0.262)]])
											.value_(1)
											.action_({ |btn|
												parentClass.set_params(player.asSymbol, \set_timeout, timeoutVal);
												timeoutUpdateContent.(btn.value);
											}),
											timeoutButton = GMRoundButton().style_(style).blinkColor_(Color.yellow).fixedWidth_(20).fixedHeight_(20).blinkTime_(0.001), nil
										).spacing_(5),
										HLayout(
											VLayout(
												HLayout(
													StaticText().font_(Font("Helvetica", 11)).maxHeight_(22).fixedWidth_(60).string_("Continuity").align_(\left).stringColor_(Color(0.837, 0.837, 0.837)),
													continuitySlider = GMFaderSlider().maxHeight_(12).fixedWidth_(60)
													.orientation_(\horizontal).min_(0).max_(10).style_(style2).value_(1.5)
													.action_({|val|
														parentClass.set_params(player.asSymbol, \continuity, val.value);
													}), nil
												),
												HLayout(
													StaticText().font_(Font("Helvetica", 11)).maxHeight_(22).fixedWidth_(60).string_("Quality").align_(\left).stringColor_(Color(0.837, 0.837, 0.837)),
													qualitySlider = GMFaderSlider().maxHeight_(12).fixedWidth_(60).orientation_(\horizontal).min_(0).max_(10).style_(style2).action_({|val|
														if (val.value == 0)
														{
															qualitySlider.backColor_(Color.green);
															parentClass.set_params(player.asSymbol, \quality, 0);
															"quality == 0".postln;
														}
														{
															qualitySlider.backColor_(Color(1, 0.71230158730159, 0, 0.79365079365079))
														};
														parentClass.set_params(player.asSymbol, \quality, val.value);

													}), nil
												),
												HLayout(
													StaticText().font_(Font("Helvetica", 11)).maxHeight_(22).fixedWidth_(60).string_("Probability").align_(\left).stringColor_(Color(0.837, 0.837, 0.837)),
													probSlider = GMFaderSlider().maxHeight_(12).fixedWidth_(60).orientation_(\horizontal).value_(10).min_(0).max_(10).style_(style2).action_({|val|
														if(val.value == 10.0)
														{
															probSlider.backColor_(Color.green);
															// probSlider.mainColor_(Color.grey) // doen't work
														}
														{
															probSlider.backColor_(Color(1, 0.71230158730159, 0, 0.79365079365079))
														};
														parentClass.set_params(player.asSymbol, \outputprobability, val.value);
													}), nil
												),
											).margins_([0, 0, 0, 0]),
										),
										HLayout(
											StaticText().font_(Font("Helvetica", 11)).maxHeight_(20).fixedWidth_(17).string_("Cut").align_(\right).stringColor_(Color(0.837, 0.837, 0.837)),
											cutButton = Button().font_(Font("Helvetica", 17)).fixedWidth_(16).fixedHeight_(16)
											.states_([["x", Color.grey, Color(0.262, 0.262, 0.262)],
												["x", Color(1, 0.7, 0, 0.8), Color(0.262, 0.262, 0.262)]])
											.value_(1).action_({|butt|
												parentClass.set_params(player.asSymbol, \cut, butt.value);
											}),
											StaticText().font_(Font("Helvetica", 11)).maxHeight_(20).fixedWidth_(60).string_("Sparse").align_(\right).stringColor_(Color(0.837, 0.837, 0.837)),
											sparseButton = Button().font_(Font("Helvetica", 17)).fixedWidth_(16).fixedHeight_(16)
											.states_([["x", Color.grey, Color(0.262, 0.262, 0.262)],
												["x", Color(1, 0.7, 0, 0.8), Color(0.262, 0.262, 0.262)]])
											.value_(0).action_({|butt|
												parentClass.set_params(player.asSymbol, \sparse, butt.value);
											}), nil
										),
										playing_mode_Button = Button().font_(Font("Helvetica", 11)).fixedWidth_(80).fixedHeight_(19).states_([["Reactive", Color(1, 0.7, 0, 0.8), Color(0.262, 0.262, 0.262)],["Continuos", Color.green, Color(0.262, 0.262, 0.262)]]).action_({|butt|
											if(butt.value == 0)
											{
												parentClass.playing_mode(player.asSymbol, 1); // manual
											}
											{
												parentClass.playing_mode(player.asSymbol,  0); // continuos
											}
										}),
										VLayout(
											HLayout(
												StaticText().font_(Font("Helvetica", 9)).maxHeight_(10).fixedWidth_(153).string_(" Active Transposition (semitones)").stringColor_(Color(0.837, 0.837, 0.837)).align_(\left), nil
											).spacing_(0).margins_([0, 0, 0, 3]),
											HLayout(
												StaticText().font_(Font("Helvetica", 7)).maxHeight_(10).fixedWidth_(180).string_(" -5    -4     -3    -2     -1     0      1      2      3     4      5      6").stringColor_(Color(0.837, 0.837, 0.837)).align_(\left)
											).spacing_(0).margins_([0, 0, 0, 0]),
											HLayout(
												{var b = GMCheckButton().style_(style_rond).fixedWidth_(13).fixedHeight_(13).displaySymbol_(\circle).action_({|pressed| transpStates[0] = if(pressed){1}{0}; if(pressed){parentClass.add_transform(player.asSymbol,-5)}{parentClass.remove_transform(player.asSymbol,-5)}}); transpButtons.add(b); b}.value,
												{var b = GMCheckButton().style_(style_rond).fixedWidth_(13).fixedHeight_(13).displaySymbol_(\circle).action_({|pressed| transpStates[1] = if(pressed){1}{0}; if(pressed){parentClass.add_transform(player.asSymbol,-4)}{parentClass.remove_transform(player.asSymbol,-4)}}); transpButtons.add(b); b}.value,
												{var b = GMCheckButton().style_(style_rond).fixedWidth_(13).fixedHeight_(13).displaySymbol_(\circle).action_({|pressed| transpStates[2] = if(pressed){1}{0}; if(pressed){parentClass.add_transform(player.asSymbol,-3)}{parentClass.remove_transform(player.asSymbol,-3)}}); transpButtons.add(b); b}.value,
												{var b = GMCheckButton().style_(style_rond).fixedWidth_(13).fixedHeight_(13).displaySymbol_(\circle).action_({|pressed| transpStates[3] = if(pressed){1}{0}; if(pressed){parentClass.add_transform(player.asSymbol,-2)}{parentClass.remove_transform(player.asSymbol,-2)}}); transpButtons.add(b); b}.value,
												{var b = GMCheckButton().style_(style_rond).fixedWidth_(13).fixedHeight_(13).displaySymbol_(\circle).action_({|pressed| transpStates[4] = if(pressed){1}{0}; if(pressed){parentClass.add_transform(player.asSymbol,-1)}{parentClass.remove_transform(player.asSymbol,-1)}}); transpButtons.add(b); b}.value,
												GMCheckButton().style_(style_rond_solo).fixedWidth_(13).fixedHeight_(13).displaySymbol_(\circle), // 0 fixe
												{var b = GMCheckButton().style_(style_rond).fixedWidth_(13).fixedHeight_(13).displaySymbol_(\circle).action_({|pressed| transpStates[5] = if(pressed){1}{0}; if(pressed){parentClass.add_transform(player.asSymbol,1)}{parentClass.remove_transform(player.asSymbol,1)}}); transpButtons.add(b); b}.value,
												{var b = GMCheckButton().style_(style_rond).fixedWidth_(13).fixedHeight_(13).displaySymbol_(\circle).action_({|pressed| transpStates[6] = if(pressed){1}{0}; if(pressed){parentClass.add_transform(player.asSymbol,2)}{parentClass.remove_transform(player.asSymbol,2)}}); transpButtons.add(b); b}.value,
												{var b = GMCheckButton().style_(style_rond).fixedWidth_(13).fixedHeight_(13).displaySymbol_(\circle).action_({|pressed| transpStates[7] = if(pressed){1}{0}; if(pressed){parentClass.add_transform(player.asSymbol,3)}{parentClass.remove_transform(player.asSymbol,3)}}); transpButtons.add(b); b}.value,
												{var b = GMCheckButton().style_(style_rond).fixedWidth_(13).fixedHeight_(13).displaySymbol_(\circle).action_({|pressed| transpStates[8] = if(pressed){1}{0}; if(pressed){parentClass.add_transform(player.asSymbol,4)}{parentClass.remove_transform(player.asSymbol,4)}}); transpButtons.add(b); b}.value,
												{var b = GMCheckButton().style_(style_rond).fixedWidth_(13).fixedHeight_(13).displaySymbol_(\circle).action_({|pressed| transpStates[9] = if(pressed){1}{0}; if(pressed){parentClass.add_transform(player.asSymbol,5)}{parentClass.remove_transform(player.asSymbol,5)}}); transpButtons.add(b); b}.value,
												{var b = GMCheckButton().style_(style_rond).fixedWidth_(13).fixedHeight_(13).displaySymbol_(\circle).action_({|pressed| transpStates[10] = if(pressed){1}{0}; if(pressed){parentClass.add_transform(player.asSymbol,6)}{parentClass.remove_transform(player.asSymbol,6)}}); transpButtons.add(b); b}.value,
											).spacing_(0).margins_(0)
										).spacing_(0).margins_(0),
										nil
									).margins_([2, 1, 0, 0]).spacing_(6),
									nil
								)
							).margins_(1)) // End parent View
						).spacing_(0).margins_([0, 0, 0, 0]),
					).spacing_(4).margins_([2, 6, 2, 0])
				),
				View.new.background_(Color(0.262, 0.262, 0.262)).layout_(
					VLayout(
						HLayout(
							StaticText().font_(Font("Helvetica", 11)).maxHeight_(20).fixedWidth_(66).string_("Time Stretch").stringColor_(Color(0.837, 0.837, 0.837)).align_(\left), // .align_(\center)
							timeStretchToggle = Button().font_(Font("Helvetica", 12)).fixedWidth_(13).fixedHeight_(13)
							.states_([["x", Color.grey, Color(0.262, 0.262, 0.262)],
								["x", Color(1, 0.7, 0, 0.8), Color(0.262, 0.262, 0.262)]])
							.action_({|butt|
								parentClass.time_stretch_on_off(player.asSymbol, butt.value);
							}),
							StaticText().font_(Font("Helvetica", 11)).maxHeight_(50).fixedWidth_(80).string_("Manual").stringColor_(Color(0.837, 0.837, 0.837)).align_(\left),
							time_stretch_slider = GMFaderSlider().maxHeight_(12).fixedWidth_(35).orientation_(\horizontal).min_(0.5).max_(2).style_(style2).value_(1).action_({ |value| // onset_threshold
								parentClass.set_time_stretch(player.asSymbol,  value);
							}),
							Button().font_(Font("Helvetica", 11)).fixedWidth_(30).fixedHeight_(15).states_([["reset", Color(0.262, 0.262, 0.262), Color(1, 0.7, 0, 0.8)]]).value_(0).action_({|butt|
								parentClass.set_time_stretch(player.asSymbol, 1);
								time_stretch_slider.value_(1)
							}), nil
						).spacing_(2),
						HLayout(
							StaticText().font_(Font("Helvetica", 11)).maxHeight_(20).fixedWidth_(60).string_("Beat align:").stringColor_(Color(0.837, 0.837, 0.837)).align_(\left), // .align_(\center)
							beatAlignButton = Button().font_(Font("Helvetica", 12)).fixedWidth_(13).fixedHeight_(13)
							.states_([["x", Color.grey, Color(0.262, 0.262, 0.262)],
								["x", Color(1, 0.7, 0, 0.8), Color(0.262, 0.262, 0.262)]])
							.action_({|butt|
								parentClass.set_params(player.asSymbol, \beat_align, butt.value);
							}), nil
						),
					).spacing_(4).margins_([2, 6, 2, 6])
				),
				View.new.background_(Color(0.262, 0.262, 0.262)).layout_(
					VLayout(
						HLayout(
							soundFile = SoundFileView().fixedHeight_(50).gridOn_(false).waveColors_([Color(0, 0.50972222222222, 1)]).peakColor_(Color(0, 0.8896164021164, 1)).rmsColor_(Color.blue), //.value_(Array.fill(13, {0})).indexThumbSize_(2.0).gap_(4);
						),
						HLayout(
							state_slider = GMFaderSlider().fixedHeight_(15).orientation_(\horizontal).min_(0).max_(100).style_(style2).value_(1).roundValue_(1).minAlpha_(1).helpersStyle_(\dot).helpersRatio_(0.25).displayHelpers_(true).helpersNumber_(10).helperSubdivisions_(7).action_({ |value| // onset_threshold
								parentClass.set_params(player.asSymbol,  \jump, value);
								value.postln;
							})
							.style_(
								GMStyle.default.deepCopy
								.helpersColor_(Color.yellow).mainColor_(Color(1, 0.7, 0, 0.8))
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
								.backColor_(Color(0.262, 0.262, 0.262))
								.backgroundColor_(Color(0.1, 0.1, 0.1))
								.disabledColor_(Color(0.5, 0.5, 0.5))
								.selectedColor_(Color(0.75, 0, 0.333))
								.helpersColor_(Color(1, 1, 1, 0.25))
								.beatColor_(Color(0, 1, 1, 0.5))
								.outlineColor_(Color(1, 0.7, 0, 0.8))
								.valueFontColor_(Color(1, 1, 1))
								.highlightColor_(Color(1, 1, 1, 0.5));
							);
						),
						HLayout(
							matchButton = GMRoundButton().style_(style).blinkColor_(Color.yellow).fixedWidth_(20).fixedHeight_(20).blinkTime_(0.001), nil
						)
					)
				),
				View.new.background_(Color(0.262, 0.262, 0.262)).layout_(
					VLayout(
						HLayout(
							StaticText().font_(Font("Helvetica", 11)).maxHeight_(22).fixedWidth_(200).string_("Outgoing Influences").align_(\center).stringColor_(Color(0.837, 0.837, 0.837))
						),
						HLayout(
							outgoingInfluenceMenu = PopUpMenu()
							.fixedHeight_(20)
							.items_(["Onset", "PitchOnset"])
							.font_(Font("Helvetica", 11))
							.background_(Color.gray(0.6))
							.action_({ |menu|
								parentClass.audio_influencer_info[player.asSymbol][5] = menu.item;
							}), nil
						),
						HLayout(
							StaticText().maxHeight_(22).fixedWidth_(120).string_("Onset threshold").font_(Font("Helvetica", 11)).align_(\right).stringColor_(Color(0.837, 0.837, 0.837)),
							onsetThresholdSlider = GMFaderSlider().maxHeight_(12).fixedWidth_(120)
							.orientation_(\horizontal).min_(0).max_(2).style_(style2).value_(onset_threshold)
							.action_({ |value|
								server.sendMsg(\n_set, onset_node, *[\threshold, value]);
							}), nil
						).spacing_(5),
						HLayout(
							StaticText().maxHeight_(22).fixedWidth_(120).string_("Onset Limiter (ms)").font_(Font("Helvetica", 11)).align_(\right).stringColor_(Color(0.837, 0.837, 0.837)),
							onsetLimiterSlider = GMFaderSlider().maxHeight_(12).fixedWidth_(120)
							.orientation_(\horizontal).min_(0).max_(500).style_(style2).value_(0.15)
							.action_({ |value|
								parentClass.audio_influencer_info[player.asSymbol][0] = value;
							}), nil
						).spacing_(5),
						HLayout(
							StaticText().maxHeight_(22).fixedWidth_(120).string_("Pitch Quality").font_(Font("Helvetica", 11)).align_(\right).stringColor_(Color(0.837, 0.837, 0.837)),
							pitchQualitySlider = GMFaderSlider().maxHeight_(12).fixedWidth_(120)
							.orientation_(\horizontal).min_(0).max_(500).style_(style2).value_(0.75)
							.action_({ |value|
								// parentClass.audio_influencer_info[player.asSymbol][0]
								/*parentClass.audio_influencer_info[player.asSymbol][0] = value;
								parentClass.audio_influencer_info[player.asSymbol].postln;
								value.postln;*/
							}), nil
						).spacing_(5),
						HLayout(
							// Button().maxHeight_(22).fixedWidth_(22).font_(Font("Helvetica", 11)).states_([["", Color.black, Color.gray], ["", Color.black, Color.green]]),
							player_blinkbutton =  GMRoundButton().style_(style).blinkColor_(Color.yellow).fixedWidth_(40).fixedHeight_(30),
							keyboard.minSize_(80@40), //	.margins_(0)
							// MultiSliderView().fixedWidth_(80).fixedHeight_(60)
							multiSlider = GMFaderMultiSlider().style_(style).backColor_(Color.black)
							// .outlineColor_(Color.blue)
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
								Color(0.3, 0.0, 1.0),     // Indigo
							]),
						).spacing_(2)
					),
				),
				UserView.new.background_(Color(0.262, 0.262, 0.262)).layout_(
					// Ligne du menu et bouton cluster
					VLayout(
						HLayout(
							StaticText().maxHeight_(22).fixedWidth_(100)
							.string_("Output Control")
							.align_(\center)
							.stringColor_(Color(0.837, 0.837, 0.837))
						),
						HLayout(
							outputMenu = PopUpMenu()
							.fixedHeight_(22)
							.items_(modes)
							.font_(Font("Helvetica", 11))
							.background_(Color.gray(0.6))
							.action_({ |menu|
								currentModeIndex = menu.value;
								setupMode.value(menu.value);
								parentClass.output(player, menu.item, offset_out, dbspec.map(currentVal)); // falta offset_out
							}),

							Button()
							.maxHeight_(22)
							.fixedWidth_(60)
							.font_(Font("Helvetica", 11))
							.states_([["Plot cluster", Color(0.34, 0.34, 0.34), Color.green]])
							.action_({
								if(current_corpus.notNil) {
									"plottter_from_text".postln;
									current_corpus.postln;
									current_corpus.class.postln;
									"plottter_from_text".postln;
									parentClass.flucoma_plotter_dico[player].win.front;
									// parentClass.flucoma_plotter(player.asSymbol, current_corpus)
								}
							}),
							PopUpMenu()
							.fixedHeight_(22)
							.items_(["reverb", "IEM", "Ambi", "rev off"])
							.font_(Font("Helvetica", 11))
							.background_(Color.gray(0.6))
							.action_({ |menu|
								case
								{menu.item == "IEM"}
								{
									parentClass.vst_master_end(player.asSymbol, "FdnReverb");
								}
								{menu.item == "rev off"}
								{
									parentClass.vst_master_end_off(player.asSymbol, "FdnReverb");
								}
							}),
							PopUpMenu()
							.fixedHeight_(22)
							.items_(["Eq", "IEM", "Eq off"])
							.font_(Font("Helvetica", 11))
							.background_(Color.gray(0.6))
							.action_({ |menu|
								case
								{menu.item == "IEM"}
								{
									parentClass.vst_master_eq(player.asSymbol, "FdnReverb");
								}
								{menu.item == "Eq off"}
								{
									parentClass.vst_master_eq_off(player.asSymbol, "MultiEQ");
								}

							}),

							nil
						),
						// Ligne contenant le NumberBox + la vue dynamique
						HLayout(
							numberBox = NumberBox()
							.font_(Font("Helvetica", 11))
							.fixedWidth_(30)
							.fixedHeight_(15)
							.step_(1)
							.clipLo_(1)
							.value_(1)
							.action_({ updateIndicators.value }), nil,

							modePanel = View()
							.layout_(HLayout()) //
							.visible_(true), nil
						),
						HLayout(
							containerView = View()
							.background_(Color(0.262, 0.262, 0.262))
							.fixedHeight_(130)
						)
					)
				)
			).spacing_(1).margins_([0, 0, 0, 0])
		);
		// win.front;

		//// OSC from audioOut Synth
		osc_meter = OSCFunc({ |msg|
			var nodeID = msg[1];
			var levels, n, pairs, audioOutNode = -1;

			// Priorité au decoder HOA si on est en mode ambisonic
/*			if(parentClass.hoa_decoder.notNil && (currentModeIndex == modes.indexOfEqual("ambisonic")))
			{
				audioOutNode = parentClass.hoa_decoder.nodeID;
			}*/
			// {
			if(parentClass.audio_group_lev[player.asSymbol].notNil)
			{
				audioOutNode = parentClass.audio_group_lev[player.asSymbol].nodeID;
			};
			// };
			// DEBUG temporaire
			// ("msg[1] nodeID: " ++ nodeID ++ " | attendu: " ++ audioOutNode ++ " | match: " ++ (nodeID == audioOutNode)).postln;

			if (nodeID == audioOutNode) {
				{
					levels = msg.copyToEnd(3);
					pairs = levels.clump(2);
					n = pairs.size.min(levelIndic.size);
					n.do { |i|
						var peakDB = pairs[i][0];
						var rmsDB  = pairs[i][1];
						var peakNorm = peakDB.ampdb.linlin(-60, 0, 0, 1).clip(0, 1);
						var rmsNorm  = rmsDB.ampdb.linlin(-60, 0, 0, 1).clip(0, 1);
						levelIndic[i].peakLevel = peakNorm;
						levelIndic[i].value     = rmsNorm;
					};
				}.defer;
			}
		}, '/meter');

		// --- FONCTION POUR METTRE À JOUR LES INDICATEURS ---
		updateIndicators = {
			var targetCount = numberBox.value.asInteger;

			// Supprimer les anciens indicateurs
			levelIndic.do { |li| li.remove };
			levelIndic = Array.fill(targetCount, { |i|
				var x = 30 + (i * spacing);
				LevelIndicator(containerView, Rect(x, 10, indicatorWidth, indicatorHeight));
			});

			// Mettre à jour les valeurs si le slider existe
			if (slider.notNil) {
				slider.action.value;
			}
		};

		// Mode selection
		setupMode = { |index|
			var numIndicators = indicatorCounts[index];
			if(slider.notNil) { currentVal = slider.value } { currentVal = dbspec.unmap(0) }; // defaut 0


			containerView.removeAll;

			slider = Slider(containerView, Rect(10, 10, 10, sliderHeight)).value_(currentVal); //dbspec.unmap(currentVal));
			dbText = StaticText(containerView, Rect(0, sliderHeight + 15, 50, 20))
			.stringColor_(Color(0.837, 0.837, 0.837))
			.align_(\left)
			.string_("– ∞ dB");

			// Lier le slider aux indicateurs + dB
			slider.action_({
				var val = slider.value;
				var audioOutNode = -1;
				if(parentClass.audio_group_lev[player.asSymbol].notNil)
				{
					audioOutNode = parentClass.audio_group_lev[player].nodeID;
					// audioOutNode.postln;
				};

				dbText.string_(dbspec.map(val.value).round(0.1));
				server.sendMsg(\n_set, audioOutNode,  *[\amp, dbspec.map(val.value)]);

			});

			// Dans la fonction setupMode :
			modePanel.removeAll;

			switch (index,
				// Mode "mono"
				modes.indexOfEqual("mono"), {
					modePanel.layout = HLayout(
						StaticText().string_("offset :").stringColor_(Color.white).font_(Font("Helvetica", 11)),
						offset_out_mono_box = NumberBox()
						.font_(Font("Helvetica", 11))
						.fixedWidth_(30)
						.fixedHeight_(15).value_(offset_out_mono).action_({ |box|
							var audioOutNode = -1;
							if(parentClass.audio_group_lev[player.asSymbol].notNil)
							{
								audioOutNode = parentClass.audio_group_lev[player].nodeID;
								// audioOutNode.postln;
							};
							server.sendMsg(\n_set, audioOutNode, *[\out, box.value]);
							offset_out_mono = box.value;
							offset_out = box.value;
						}), nil
					);
				},
				// Mode "stereo"
				modes.indexOfEqual("stereo"), {
					modePanel.layout = HLayout(
						StaticText().string_("offset 0 ").stringColor_(Color.white).font_(Font("Helvetica", 11)),
						offset_out_stereo0_box = NumberBox()
						.font_(Font("Helvetica", 11))
						.fixedWidth_(30)
						.fixedHeight_(15).value_(offset_out_stereo0).action_({ |box|
							var audioOutNode = -1;
							if(parentClass.audio_group_lev[player.asSymbol].notNil)
							{
								audioOutNode = parentClass.audio_group_lev[player].nodeID;
								// audioOutNode.postln;
							};
							server.sendMsg(\n_set, audioOutNode,  *[\out0, box.value]);
							offset_out_stereo0 = box.value;
							offset_out = box.value
						}),
						StaticText().string_("offset 1 ").stringColor_(Color.white).font_(Font("Helvetica", 11)),
						offset_out_stereo1_box = NumberBox()
						.font_(Font("Helvetica", 11))
						.fixedWidth_(30)
						.fixedHeight_(15).value_(offset_out_stereo1).action_({ |box|
							var audioOutNode = nil;
							if(parentClass.audio_group_lev[player.asSymbol].notNil)
							{
								audioOutNode = parentClass.audio_group_lev[player].nodeID;
								// audioOutNode.postln;
							};
							server.sendMsg(\n_set, audioOutNode, *[\out1, box.value]);
							offset_out_stereo1 = box.value;
							offset_out2 = box.value;
						})
					);
				},

				// Mode "aoo"
				modes.indexOfEqual("aoo"), {
					parentClass.flucoma_labels(player.asSymbol, current_corpus); // clustering
					modePanel.layout = HLayout(
						StaticText()
						.font_(Font("Helvetica", 11))
						.string_("offset ")
						.stringColor_(Color.white),
						offset_out_aoo_box = NumberBox()
						.font_(Font("Helvetica", 11))
						.fixedWidth_(30)
						.fixedHeight_(15)
						.step_(1)
						.clipLo_(0)
						.value_(offset_out_aoo)
						.action_({ |box|
							var audioOutNode = -1;
							if(parentClass.audio_group_lev[player.asSymbol].notNil)
							{
								audioOutNode = parentClass.audio_group_lev[player].nodeID;
								// audioOutNode.postln;
							};
							server.sendMsg(\n_set, audioOutNode,  *[\out, box.value]);
							offset_out_aoo  = box.value;
							offset_out = box.value;
						}), nil
					);
				},

				// Mode "ambisonic"
				modes.indexOfEqual("ambisonic"), {
					modePanel.layout = HLayout(
						StaticText().string_("offset ").stringColor_(Color.white).font_(Font("Helvetica", 11)),
						offset_out_ambisonic_box = NumberBox().font_(Font("Helvetica", 11))
						.fixedWidth_(30)
						.fixedHeight_(15)
						.value_(offset_out_ambisonic).action_({ |box|
							var audioOutNode = -1;
							if(parentClass.audio_group_lev[player.asSymbol].notNil)
							{
								// audioOutNode = parentClass.audio_group_lev.nodeID; // Decoder ID
								audioOutNode = parentClass.audio_group_lev[player.asSymbol].nodeID;
								// audioOutNode.postln;
							};
							server.sendMsg(\n_set, audioOutNode, *[\out, box.value]);
							offset_out_ambisonic = box.value;
							offset_out = box.value;
						}),
						StaticText().string_("order ").stringColor_(Color.white).font_(Font("Helvetica", 11)),
						NumberBox()
						.font_(Font("Helvetica", 11))
						.fixedWidth_(30)
						.fixedHeight_(15).value_(3).step_(1).clipLo_(1).clipHi_(7).action_({ |box|
							("Ordre Ambisonic : " ++ box.value).postln
						}),
						PopUpMenu()
						.fixedHeight_(15)
						.fixedWidth_(90)
						.items_([
							"random",
							"front",
							"back",
							"left",
							"right",
							"up",
							"down",
							"center",
							"front_left",
							"front_right",
							"back_left",
							"back_right",
							"circle_h",
							"dome",
							"rotating"
						])
						.font_(Font("Helvetica", 11))
						.background_(Color.gray(0.6))
						.action_({ |menu|
							parentClass.set_ambi_distribution(player.asSymbol, menu.item.asSymbol);
						}), nil
					).spacing_(2)
				},

				// Mode "multichannel"
				modes.indexOfEqual("multichannel"), {
					modePanel.layout = HLayout(
						StaticText().string_("offset :").stringColor_(Color.white).font_(Font("Helvetica", 11)),
						offset_out_multichannel_box = NumberBox().font_(Font("Helvetica", 11))
						.fixedWidth_(30)
						.fixedHeight_(15)
						.value_(offset_out_multichannel).action_({ |box|
							var audioOutNode = -1;
							if(parentClass.audio_group_lev[player.asSymbol].notNil)
							{
								audioOutNode = parentClass.audio_group_lev[player].nodeID;
								// audioOutNode.postln;
							};
							server.sendMsg(\n_set, audioOutNode, *[\out, box.value]);
							offset_out_multichannel = box.value;
							offset_out = box.value;
						}), nil
					);
				},

				{
					modePanel.layout = nil; // vide
				}
			);

			// Affichage mode "AOO"
			/*if (index == modes.indexOfEqual("aoo")) {
			offset_out_box.valueAction_(offset_out);
			aooPanel.visible = true;
			} {
			offset_out_box.valueAction_(0);
			aooPanel.visible = false;
			};*/
			// Mettre à jour le numberBox (ce qui déclenche updateIndicators)
			numberBox.value = numIndicators;
			updateIndicators.value;
		};
		// default "stereo"
		outputMenu.value = 1; // 1 stereo 3 ambisonic
		setupMode.value(1);

		// Init timeout
		timeoutUpdateContent.(1);
	}

	load_soundFile {|soundfilePath, outnode, num_segs, onset_descriptor|
		var sf;

		audioOut_synth_node = outnode;
		onset_node = onset_descriptor;
		soundfilePath.postln;
		sf = SoundFile.openRead(soundfilePath.asString); // for SoundFileView
		soundFile.soundfile = sf;            // set soundfile
		defer { state_slider.max_(num_segs) };  // slider state
		{0.1.wait;{
			soundFile.readWithTask(0, sf.numFrames);     // read in the entire file.
			soundFile.refresh;                  // refresh to display the file.

		}.defer}.fork;
		menuButton.style_(style4); // ready
		menuButton.blink; // blink
	}
	loading {
		menuButton.style_(style); // init style
		blinking = Routine {
			loop {
				menuButton.blink;
				0.5.wait;
			}
		}.play
	}
	// From influencer
	analyse {|pitch, chroma_array, mfcc_array|
		influ_onset_button.blink;
		defer {
			influ_mfcc.values_(mfcc_array);
			influ_chroma.values_(chroma_array);
			influ_pitch.value_(pitch);
		}
	}

	// From analyse
	chroma {|chroma_array|
		defer {
			multiSlider.values_(chroma_array);
		}
	}
	mfcc {|mfcc_array|
		defer {
			multiSlider_mfcc.values_(mfcc_array);
		}
	}
	pitch {|pitch|
		keyboard.playKey(pitch%12);
	}

	lev {|level|
		slider.value_(dbspec.unmap(level));
		dbText.string_(level.round(0.1));
	}

	blink {
		/*	"blink".postln;
		pitch.postln;*/
		player_blinkbutton.blink;
		// keyboard.playKey(pitch%12);
/*		{AppClock.sched(0, {
			// multiSlider.values_(chroma_array);
			multiSlider_mfcc.values_(mfcc_array);
		})}.fork;*/
	}
	corpus_menu_items {|itm|
		"TOTOTOTO".postln;
		defer { corpus_menu.items_(itm);}
	}
	setmenuitem {|itm, corpus|
		defer { corpus_menu.value_(itm);
			current_corpus = corpus;
		}
	}
	state {|num|
		// defer { corpus_menu.value_(itm);}
		defer { state_slider.value_(num);} // slider state
	}
	match {|num|
		/*		"match".postln;
		num.postln;
		num.class.postln;
		"match".postln;*/
		case
		{num == \match}
		{
			matchButton.style_(style4); //green
			matchButton.blinkColor_(Color(0.36170634920635, 1, 0, 0.84920634920635));
			matchButton.blink; // blink
		}
		{num == \no_match}
		{
			matchButton.style_(style3); //
			matchButton.blinkColor_(Color(0.837, 0.837, 0.837));
			matchButton.blink; // blink
		}
		{num == \fallback}
		{
			matchButton.style_(style2); //
			matchButton.blinkColor_(Color(1, 0.71230158730159, 0, 0.79365079365079));
			matchButton.blink; // blink
		}
		{num == \continue}
		{
			timeoutButton.style_(style2); //
			timeoutButton.blinkColor_(Color(1, 0.71230158730159, 0, 0.79365079365079));
			timeoutButton.blink; // blink
		}
		{num == \timeout}
		{
			timeoutButton.style_(style); //
			timeoutButton.blinkColor_(Color(1, 0.71230158730159, 0, 0.79365079365079));
			timeoutButton.blink; // blink
		}
		{num == \timeout_reset}
		{
			timeoutButton.style_(style); //
			timeoutButton.blinkColor_(Color(1, 0.71230158730159, 0, 0.79365079365079));
			timeoutButton.blink; // blink
		}

	}
}

