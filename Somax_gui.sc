Somax_gui {
	classvar somax_obj, server;
	var 	<>wind, hview, auxreceiveview, server, plotwin, plotcomposite, interfaces_names, busview, selitem, moduleview, selectrack, brwwind, paramsview, slidwin, contextSlider, param_numb, moduleitem, selparam, influenceritem, <>influencerview, influencercount = 0, agentitem, <>agentview, agentcount = 1, <>influencer_playerview, selectinfluencer, selectinfluencer_player, selectagent, indexOfList, removeAtList, influencer_menu_state, corpus_path, symb_influencer, symb_player, <>path_string, dlI, dlO, <>server_button, <>runToggle, <>runButton, style,<> server_status, agentcolors, renamewin, renametext;

	var audio_influencer_dico; // Somax Class

	var <>inDeviceMenu, <>outDeviceMenu; // Config

	*new {|somax|
		^super.newCopyArgs(somax).init(somax);
	}

	init {|somax|
		somax_obj = somax;
		// somax_obj = somax.new; // Somax objet
		// somax.new;
		somax_obj.postln;
		somax_obj.class.postln;
		// influencer = List[];
		// agent = List[];
		audio_influencer_dico = Dictionary.new;
		server = Server.default;

		indexOfList = {|a, b|
			a.collect({arg item ;
				b.indexOf(item)
			})
		};

		removeAtList = {|a, b|
			b.collect({arg item ;
				a.removeAt(item)
			})
		};
		dlI = ServerOptions.inDevices.collect(_.asSymbol);
		dlO = ServerOptions.outDevices.collect(_.asSymbol);

		agentcolors = List.new;

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

		/*		"CREATE_AGENT".postln;
		somax_obj.model_server[\params][\create_agent].postln;
		"CREATE_AGENT".postln;*/


		wind =Window("Somax2Collider", Rect(385, 550, 300, 500))
		.background_(Color.new255(120, 120, 120))
		.alpha_(0.9)
		.layout_( VLayout(
			View()   //.maxHeight_(50).fixedWidth_(308)
			.background_(Color.new255(120, 100, 120, 100)).layout_(
				VLayout(
					HLayout(
						// GridLayout.rows(
						/*    [*/
						View().background_(Color.new255(120, 120, 120)).layout_(
							VLayout(
								server_button = Button().maxHeight_(22).fixedWidth_(120).font_(Font("Helvetica", 11)).states_([["Somax Server", Color(1, 1, 1, 0.7), Color.gray], ["Somax Server", Color(0.4, 0.4, 0.4), Color.green]]).value_(somax_obj.model_server[\params][\somax_server_status]).action_({|val|
									if(val.value == 1) {
										// brwwind = TBrowser.new(influencer)
										somax_obj.start;
										somax_obj.postln;
										somax_obj.class.postln;
										somax_obj.players.postln;
										somax_obj.model_server.setParam(\somax_server_status, 1)
									}{
										// brwwind.win.close;
										somax_obj.exit;
									}
								}), nil,
								HLayout(
									runToggle= Button().fixedWidth_(30).fixedHeight_(30).states_([["X", Color(0.27, 0.27, 0.27), Color.grey], ["X", Color.yellow, Color.grey]]).font_(Font("Helvetica", 30)).value_(somax_obj.model_server[\params][\somax_server_run]).action_({|val|
										if(val.value == 1)
										{
											somax_obj.run;
											somax_obj.model_server.setParam(\somax_server_run, 1)
										}
										{
											somax_obj.stop;
											somax_obj.model_server.setParam(\somax_server_run, 0)
										}

									}),
									//nil,
									StaticText().string_("beat").fixedWidth_(20).stringColor_(Color(1, 1, 1, 0.7)).font_(Font("Times", 12)),
									runButton = GMRoundButton().style_(style).blinkColor_(Color.yellow).fixedWidth_(20).fixedHeight_(20), nil, nil
								).spacing_(10),
								nil,
							).spacing_(0).margins_([0, 0, 0, 0])
						),
						View().background_(Color.new255(120, 120, 120)).layout_(
							VLayout(
								server_status = Button().maxHeight_(22).fixedWidth_(120).font_(Font("Helvetica", 11)).states_([["Boot SC Server", Color(1, 1, 1, 0.7), Color.gray], ["Boot SC Server", Color(0.4, 0.4, 0.4), Color.green]]).action_({|val|
									if(val.value == 1) {
										server.boot;
										server.waitForBoot({
											// ~scservers.add(\local -> s);
											// ~scservers_groups.add(\local -> List[]);
										})
									}{
										server.quit;
									}
								}), nil,
								VLayout(
									HLayout(
										StaticText().string_("In").fixedWidth_(22).stringColor_(Color(1, 1, 1, 0.7)).font_(Font("Times", 12)),
										inDeviceMenu = PopUpMenu().maxHeight_(15).fixedWidth_(120).items_(dlI).font_(Font("Helvetica", 8)).background_(Color.gray(0.6)).stringColor_(Color(1, 1, 1, 0.7)).action_({|menu|
											server.options.inDevice = menu.item;
											somax_obj.save_session;
											/*
											~serverInDevice = menu.item;
											file_deviceI = File.new(file_device_pathI, "w+");
											file_deviceI.write(menu.item.asString);
											file_deviceI.close;*/
											// server.reboot;

										})
									),
									HLayout(
										StaticText().string_("Out").fixedWidth_(22).stringColor_(Color(1, 1, 1, 0.7)).font_(Font("Times", 12)),
										outDeviceMenu = PopUpMenu().maxHeight_(15).fixedWidth_(120).items_(dlO).font_(Font("Helvetica", 8)).background_(Color.gray(0.6)).stringColor_(Color(1, 1, 1, 0.7)).action_({|menu|
											server.options.outDevice = menu.item;
											somax_obj.save_session;
											/*
											~serverOutDevice = menu.item;
											file_deviceO = File.new(file_device_pathO, "w+");
											file_deviceO.write(menu.item.asString);
											file_deviceO.close;*/
											// server.reboot;

										})
									)
								)
							).spacing_(5).margins_([0, 0, 0, 0])
						)
						// ],
						// nil
					).margins_([0, 0, 0, 0]),
			).margins_([0, 0, 0, 0])), //.spacing_(1)), // margins_([0, 0, 0, 0])
			VLayout(
				HLayout(
					Button().maxHeight_(22).fixedWidth_(80).font_(Font("Helvetica", 11)).states_([["Corpus path", Color(1, 1, 1, 0.7), Color.gray]]).action_({
						FileDialog({ |path|
							postln("Corpus path" + path);
							somax_obj.corpus_path(path);
							corpus_path = path;
							// path_string.string = "Corpus path :"++corpus_path;
							somax_obj.model_server.setParam(\corpus_path, corpus_path);
							somax_obj.save_session;
						},
						fileMode: 0,
						stripResult: true,
						path: Platform.userAppSupportDir);
					}),
					nil,
					path_string = StaticText().string_(somax_obj.model_server[\params][\corpus_path]).stringColor_(Color(1, 1, 1, 0.7)).font_(Font("Times", 9)),
					nil),
			).spacing_(1), // margins_([0, 0, 0, 0])
			hview = View()   //.maxHeight_(50).fixedWidth_(308)
			.background_(Color.red).layout_(
				VLayout(
					HLayout(
						Button().maxHeight_(22).fixedWidth_(60).font_(Font("Helvetica", 11)).states_([["Connect", Color(0.8, 0.8, 0.8), Color.new255(50, 255, 50, 100)]]).action_({
							case
							{influencer_menu_state == 1} // Influencers
							{
								agentview.items[selectagent].do({|agenitem| // loop selected agent items
									influencerview.items[selectinfluencer].do({|influitem| // loop selected influencer items
										audio_influencer_dico[influitem] = audio_influencer_dico[influitem].add(agenitem); // update dico
										somax_obj.connection(influitem, agenitem); // connect
									});
									somax_obj.player_gui_dico[agenitem].influence_source.items_(somax_obj.players[agenitem].drop(1).asArray); // add influencer to player gui
								});
								"audio_influencer_dico".postln;
								audio_influencer_dico.postln;
							}
							{influencer_menu_state == 2} // Agents as Influencers
							{
								// "audio_influencer###1".postln;
								agentview.items[selectagent].do({|agenitem| // loop selected agent items
									influencer_playerview.items[selectinfluencer_player].do({|influitem| // loop selected players as influencer items
										audio_influencer_dico[influitem] = audio_influencer_dico[influitem].add(agenitem); // update dico
										somax_obj.connection(influitem, agenitem); // connect
									});
									somax_obj.player_gui_dico[agenitem].influence_source.items_(somax_obj.players[agenitem].drop(1).asArray); // add influencer to player gui
								});

								/*								audio_influencer_dico[influencer_playerview.item.asSymbol] = audio_influencer_dico[influencer_playerview.item.asSymbol].add(agentview.items[agentview.selection]).flat;
								audio_influencer_dico.postln;*/
							}
						}),
						Button().maxHeight_(22).fixedWidth_(60).font_(Font("Helvetica", 11)).states_([["Disconnect", Color(0.8, 0.8, 0.8), Color.new255(50, 50, 255, 100)]]).action_({
							var res_array;
							case
							{influencer_menu_state == 1}
							{
								agentview.items[selectagent].do({|agenitem| // loop selected agent items
									influencerview.items[selectinfluencer].do({|influitem| // loop selected influencer items
										audio_influencer_dico[influitem] = audio_influencer_dico[influitem].removeAll(agenitem); // update dico
										somax_obj.disconnection(influitem, agenitem); // connect
									});
									somax_obj.player_gui_dico[agenitem].influence_source.items_(somax_obj.players[agenitem].drop(1).asArray); // remove influencer to player gui
								});
							}
							{influencer_menu_state == 2}
							{
								agentview.items[selectagent].do({|agenitem| // loop selected agent items
									influencer_playerview.items[selectinfluencer_player].do({|influitem| // loop selected influencer items
										audio_influencer_dico[influitem] = audio_influencer_dico[influitem].removeAll(agenitem); // update dico
										somax_obj.disconnection(influitem, agenitem); // connect
									});
									somax_obj.player_gui_dico[agenitem].influence_source.items_(somax_obj.players[agenitem].drop(1).asArray); // remove influencer to player gui
								});
							}
							// removeAtList.(agentview.items, agentview.selection);
							// agentview.items.postln;
						}), nil
					),
					HLayout(
						View().background_(Color.rand).layout_(
							VLayout(
								StaticText().string_("Influencers").stringColor_(Color(0.8, 0.8, 0.8)),
								HLayout(
									Button().maxHeight_(22).fixedWidth_(60).font_(Font("Helvetica", 11)).states_([["New Audio", Color(0.34, 0.34, 0.34), Color.green]]).action_({
										somax_obj.audio_influencer((\AudioInfluencer_++influencercount).asSymbol, \audioIn, 0); // audio In init
										// influencer.insert(influencercount, (\AudioInfluencer_++(influencercount+1)).asSymbol -> 0);
										// influencerview.items = influencer.collect({|x| x.key});
										influencerview.selection_([]);
										influencercount = influencercount + 1;
										// somax_obj.audio_influencer(\AudioInfluencer_++influencercount, \audioIn, 0); // audio In

									}),
									Button().maxHeight_(22).fixedWidth_(50).font_(Font("Helvetica", 11)).states_([["Delete", Color(0.34, 0.34, 0.34), Color.gray(0.6)]]).action_({
										if (somax_obj.influencerl.size > 0) {
											// somax_obj.audio_influencer_info.postln;
											influencerview.items[selectinfluencer].do({|influitem, i|
												if (somax_obj.influencer_gui_dico.includesKey(influitem)) // if window open
												{
													somax_obj.influencer_gui_dico[influitem].win.close;
												};
												"DELELELELTETET".postln;
												influitem.postln;
												somax_obj.audio_influencer_info.postln;
												somax_obj.audio_influencer_info[influitem][2].postln;
												somax_obj.audio_influencer_info[influitem][2].class.postln;
												"DELELELELTETET".postln;
												if((somax_obj.audio_influencer_info[influitem][2] == \audioIn) || (somax_obj.audio_influencer_info[influitem][2] == \soundFile) || (somax_obj.audio_influencer_info[influitem][2] == \inputSound))
												{
													"DELELELELTETET2222".postln;
													somax_obj.audio_influencer(influitem, "off"); // remove audio In
												};
												// somax_obj.influencer_gui_dico.postln;

												// somax_obj.influencerl(agentitem); // remove agent
												audio_influencer_dico.removeAt(influitem);
											});
											/*										selectinfluencer.collect({|idx, i| // remove from an index
											var index = idx - i;
											if (index >= 0 && index < somax_obj.influencerl.size)
											{
											somax_obj.influencerl.removeAt(index);
											}
											});*/
											// somax_obj.influencerview.items = somax_obj.influencerl.collect({|x| x.key});
										}{

											influencerview.items = [];
											somax_obj.influencerl = List[];
										}
										/*if (influencer.size != 0) {
										selectinfluencer.collect({|idx, i| // remove from an index
										influencer.removeAt(idx-i);
										influencerview.items = influencer.collect({|x| x.key});
										"influencerview.items".postln;
										influencerview.items.postln;
										"idx.".postln;
										idx.postln;
										audio_influencer_dico.removeAt(influencerview.item);
										somax_obj.audio_influencer( (\AudioInfluencer_++(influencercount)).asSymbol, \audioIn, 0); // audio In
										})
										}{

										influencerview.items = [];
										influencer = List[];
										}*/
									}), nil
								),
								HLayout(
									Button().maxHeight_(22).fixedWidth_(60).font_(Font("Helvetica", 11)).states_([["New Midi", Color(0.34, 0.34, 0.34), Color.green]]).action_({
										somax_obj.influencerl.insert(influencercount, (\Midi_Influencer_++(influencercount+1)).asSymbol -> 0);
										somax_obj.influencerview.items = somax_obj.influencerl.collect({|x| x.key});
										somax_obj.influencerview.selection_([]);
										somax_obj.influencercount = influencercount + 1;
									}),nil
								),
								influencerview = ListView() /////////////////////////////////////influencer Synth/////////////////////////////////
								// .fixedWidth_(100)
								// .items_(influencer.asArray)
								.items_(somax_obj.influencerl.asArray.reverse)
								.background_(Color.black(0.6))
								.stringColor_(Color.green(1))
								.alpha_(1)
								.hiliteColor_(Color.red(0.6))
								.selectedStringColor_(Color.green(1))
								.selectionMode_(\extended)
								.beginDragAction_({|args|
									influencerview.items[influencerview.selection] // Drag selected items
								})
								.selectionAction_({|sbs|
									var  symb_res;
									if (somax_obj.influencerl.size != 0) {
										// "influencerview111".postln;
										influencer_menu_state = 1;
										// sbs.value.postln;
										selectinfluencer= influencerview.selection;

										symb_influencer = influencerview.items[sbs.value];
										// symb_influencer.postln;
										// influencerview.items[sbs.value].class.postln;
										// audio_influencer_dico.postln;
										symb_res = audio_influencer_dico[symb_influencer];
										// symb_res.postln;
										// agentview.items.postln;
										agentview.selection_(indexOfList.(symb_res, agentview.items)); // select connected agents
										influencer_playerview.selection_([]);
									}
								})
								.mouseDownAction_({|uvw, x, y,modifiers, buttonNumber, clickCount| //double-click
									if (clickCount == 2) {
										if(somax_obj.influencer_gui_dico.includesKey(symb_influencer.asSymbol))
										{
											"influencers_window###".postln;
											somax_obj.influencer_gui_dico[influencerview.items[influencerview.selection][0]].win.front;
										}
										{
											somax_obj.influencer_gui(symb_influencer.asSymbol);
										}
									};
								}),
								influencer_playerview = ListView() /////////////////////////////////////influencer Synth/////////////////////////////////
								// .fixedWidth_(100)
								.items_(somax_obj.model_server[\params][\create_agent])
								.background_(Color.black(0.6))
								.stringColor_(Color.green(1))
								.alpha_(1)
								.hiliteColor_(Color.red(0.6))
								.selectedStringColor_(Color.green(1))
								.selectionMode_(\extended)
								.beginDragAction_({|args|
									influencer_playerview.items[influencer_playerview.selection] // Drag selected items
								})
								.selectionAction_({|sbs|
									var symb, symb_res;
									if (somax_obj.agent.size > 0) {
										influencer_menu_state = 2;
										selectinfluencer_player = influencer_playerview.selection;

										influencer_playerview.items[sbs.value].postln;
										///
										symb = influencer_playerview.items[sbs.value];
										symb_res = audio_influencer_dico[symb];
										agentview.selection_(indexOfList.(symb_res, agentview.items)); // select connected agents
										///
										influencerview.selection_([]);
									}

								}),
						))
						,
						View().background_(Color.rand).layout_(
							VLayout(
								StaticText().string_("Player (agents)").stringColor_(Color(0.8, 0.8, 0.8)),
								HLayout(
									Button().maxHeight_(22).fixedWidth_(60).font_(Font("Helvetica", 11)).states_([["New Agent", Color(0.34, 0.34, 0.34), Color.green]]).action_({
										// agent.insert(agentcount, (\Agent_++(agentcount+1)).asSymbol -> 0);
										somax_obj.create_agent( (\Agent_++(agentcount)).asSymbol); // create audio agent

										// agentview.items = agent.collect({|x| x.key});
										// influencer_playerview.items = agent.collect({|x| x.key});
										influencer_playerview.selection_([]);
										agentview.selection_([]);
										agentcount = agentcount + 1;
										"Agent_create".postln;
										somax_obj.agent.postln;
									}),
									Button().maxHeight_(22).fixedWidth_(50).font_(Font("Helvetica", 11)).states_([["Delete", Color(0.34, 0.34, 0.34), Color.gray(0.6)]]).action_({
										if (somax_obj.agent.size > 0) {

											selectagent.reverse.do({ |index|
												agentcolors.removeAt(index);  // remove list items colors
												agentcolors.postln;
											});

											agentview.items[selectagent].do({|agentitem, i|
												if (somax_obj.player_gui_dico.includesKey(agentitem)) // if window open
												{
													somax_obj.player_gui_dico[agentitem].win.close;
													if(somax_obj.flucoma_plotter_dico[agentitem].notNil)
													{
														somax_obj.flucoma_plotter_dico[agentitem].win.close; //
													}
												};
												somax_obj.delete_agent(agentitem); // remove agent
												audio_influencer_dico.removeAt(agentitem);
											});
											agentview.colors_(agentcolors.asArray); // Update ListView items colors
										}{

											agentview.items = [];
											somax_obj.agent = List[];
										}
									}), nil
								),
								HLayout(
									Button().maxHeight_(22).fixedWidth_(50).font_(Font("Helvetica", 11)).states_([["Rename", Color.black, Color.red]]).action_({|val|
										if ((val.value != nil )&& (agentview.items[selectagent][0] != nil)) {
											var player_name;
											player_name = agentview.items[selectagent][0];
											renamewin = Window.new(border:false).front;
											renamewin.bounds = Rect(wind.bounds.left + 235, wind.bounds.top + 295 ,145, 22);
											renamewin.alpha_(0.8);

											renametext = TextField(renamewin)
											.background_(Color.white)
											.string_(player_name.asString)
											.action_({|txt|
												somax_obj.delete_agent(player_name.asSymbol);
												if (somax_obj.player_gui_dico.includesKey(player_name)) // if window open
												{
													somax_obj.player_gui_dico[player_name].win.close;
												};
												somax_obj.create_agent(txt.value.asSymbol);
												renamewin.close;
												// renametext.remove
											})
										}
									}), nil
									/*									Button().maxHeight_(22).fixedWidth_(60).font_(Font("Helvetica", 11)).states_([["New Midi", Color(0.4, 0.4, 0.4), Color.green]]).action_({
									agent.insert(agentcount, (\Midi_agent_++(agentcount+1)).asSymbol -> 0);
									agentview.items = agent.collect({|x| x.key});
									influencer_playerview.items = agent.collect({|x| x.key});
									influencer_playerview.selection_([]);
									agentview.selection_([]);
									agentcount = agentcount + 1;
									}), nil*/
								),

								agentview = ListView() /////////////////////////////////////players /////////////////////////////////
								// .fixedWidth_(100)
								// .items_(somax_obj.model_server[\params][\create_agent])
								.items_(somax_obj.agent.asArray.reverse)
								.background_(Color.black)
								.stringColor_(Color.black) //(0, 0.12982804232804, 1) //(Color.new255(238, 154, 0))               //Color.blue(1))
								.hiliteColor_(Color(0, 0.17731481481482, 1)) //Color.blue(0.5))
								.selectedStringColor_(Color(0.8, 0.8, 0.8)) //(Color.new255(139, 90, 0))     //Color.blue(1))
								.selectionMode_(\extended)
								.selectionAction_({|sbs|
									if (somax_obj.agent.size != 0) {
										selectagent= agentview.selection; // index selected elements
										// agentview.items[sbs.value].postln;
										symb_player = agentview.items[sbs.value];
									}
								})
								.mouseDownAction_({|uvw, x, y,modifiers, buttonNumber, clickCount| //double-click
									if (clickCount == 2) {
										if(somax_obj.player_gui_dico.includesKey(symb_player.asSymbol))
										{
											somax_obj.player_gui_dico[agentview.items[agentview.selection][0]].win.front;
										}
										{
											// somax_obj.player_gui_dico[agentview.items[agentview.selection][0]].win.front;

											somax_obj.player_gui(symb_player.asSymbol); // create new gui
										}
									};
								})
								.keyDownAction_({ |doc, char|
									if (char.asString == "e") { // enable agent
										agentview.items[selectagent].do({|agentitem|
											somax_obj.set_params(agentitem.asSymbol,  \enabled, 1); // send to somax_obj
										});
										selectagent.do({ |index|
/*											index.postln;
											"index".postln;*/
											agentcolors[index] = Color(0.40919312169312, 1, 0, 0.70634920634921);  // Vert
										});
										agentview.colors_(agentcolors.asArray); // Update ListView items colors
										agentview.selection = []; // remove selection
									};
									// Si la touche est 'd', changer la couleur des éléments sélectionnés en rouge
									if (char.asString == "d") { // disable agent
										agentview.items[selectagent].do({|agentitem|
											somax_obj.set_params(agentitem.asSymbol,  \enabled, 0); // send to somax_obj
										});
										selectagent.do({ |index|
											agentcolors[index] = Color.grey //(1.0, 0.25625, 0.25625);  // Rouge
										});
										agentview.colors_(agentcolors.asArray);   // Update ListView items colors
										agentview.selection = []; // remove selection
									};
									if (char.asString == "o") {  // open agent window
										agentview.items[selectagent].do({|agentitem|
											somax_obj.player_gui_dico[agentitem].win.front;
										});
									};
									if (char.asString == "w") { // close agent window
										agentview.items[selectagent].do({|agentitem|
											somax_obj.player_gui_dico[agentitem].win.close;
										});
									};
									if (char.asString == "c") { // connect

										agentview.items[selectagent].do({|agenitem| // loop selected agent items
											influencerview.items[selectinfluencer].do({|influitem| // loop selected influencer items
												audio_influencer_dico[influitem] = audio_influencer_dico[influitem].add(agenitem); // update dico
												somax_obj.connection(influitem, agenitem); // connect
											});
											somax_obj.player_gui_dico[agenitem].influence_source.items_(somax_obj.players[agenitem].drop(1).asArray); // add influencer to player gui
										});
									};
									if (char.asString == "x") { // disconnect
										agentview.items[selectagent].do({|agenitem| // loop selected agent items
											influencerview.items[selectinfluencer].do({|influitem| // loop selected influencer items
												audio_influencer_dico[influitem] = audio_influencer_dico[influitem].removeAll(agenitem); // update dico
												somax_obj.disconnection(influitem, agenitem); // connect
											});
											somax_obj.player_gui_dico[agenitem].influence_source.items_(somax_obj.players[agenitem].drop(1).asArray); // remove influencer to player gui
										});
									};
								})

						))


						/*~wind1 = ListView() //Buses
						//.items_(~interfaces.keys.asArray)
						.fixedWidth_(100)
						.background_(Color.gray(0.6))
						.hiliteColor_(Color.green(alpha:0.6))
						.action_({ arg sbs;
						//[sbs.value, v.items[sbs.value]] // .value returns the integer
						})
						.canReceiveDragHandler_({ View.currentDrag.isString })
						.beginDragAction = { arg listView;
						//listView.items[ listView.value ].debug("begun dragging");
						},
						~wind2 = ListView() /////////////////////////////////////influencer OTROS /////////////////////////////////
						.fixedWidth_(100)
						.items_()
						.background_(Color.gray(0.6))
						.hiliteColor_(Color.green(alpha:0.6))
						.action_({ arg sbs;

						// influenceritem = influencer[influencerview.items[sbs.value]];
						//influenceritem = influencer.collect({|x| x.value})[sbs.value];
						//influenceritem.postln;


						})
						.canReceiveDragHandler_({ View.currentDrag.isString })
						.beginDragAction_({ arg listView;
						//listView.items[ listView.value ].debug("begun dragging");
						~tgraph;
						//[\g_02_TTestSynth, \t_01_TMFxRev ]
						//[0, 1, 2, 3];
						})
						.selectionMode_(\extended)
						.keyDownAction_({|view, char, modifiers, unicode, keycode, key|
						char.postln})
						.mouseDownAction_({|uvw, x, y,modifiers, buttonNumber, clickCount| //double-click
						if (clickCount == 2) {
						//influenceritem.win.front;

						};
						//if ((clickCount == 1) && (~interfaces.size == 1)) {"totototototooto".postln; ~tgraphsynthslist.value = nil};
						}),
						~wind3 = ListView() /////////////////////////////////////influencer OTROS /////////////////////////////////
						.fixedWidth_(100)
						.items_()
						.background_(Color.gray(0.6))
						.hiliteColor_(Color.green(alpha:0.6))
						.action_({ arg sbs;

						// influenceritem = influencer[influencerview.items[sbs.value]];
						//influenceritem = influencer.collect({|x| x.value})[sbs.value];
						//influenceritem.postln;


						})
						.canReceiveDragHandler_({ View.currentDrag.isString })
						.beginDragAction_({ arg listView;
						//listView.items[ listView.value ].debug("begun dragging");
						~tgraph;
						//[\g_02_TTestSynth, \t_01_TMFxRev ]
						//[0, 1, 2, 3];
						})
						.selectionMode_(\extended)
						.keyDownAction_({|view, char, modifiers, unicode, keycode, key|
						char.postln})
						.mouseDownAction_({|uvw, x, y,modifiers, buttonNumber, clickCount| //double-click
						if (clickCount == 2) {
						//influenceritem.win.front;

						};
						//if ((clickCount == 1) && (~interfaces.size == 1)) {"totototototooto".postln; ~tgraphsynthslist.value = nil};
						})
						*/
			)), nil),
			View()   //.maxHeight_(50).fixedWidth_(308)
			.background_(Color.black).layout_(
				VLayout(
					HLayout(
						Button().maxHeight_(22).fixedWidth_(150).font_(Font("Helvetica", 11)).states_([["Get Influencers", Color(0.34, 0.34, 0.34), Color.green]]).action_({
							somax_obj.get_influencers;
						}),
						Button().maxHeight_(22).fixedWidth_(150).font_(Font("Helvetica", 11)).states_([["Get Players", Color(0.34, 0.34, 0.34), Color.green]]).action_({
							somax_obj.get_players;
						}),
					)
				)
			)
		).spacing_(5).margins_([5, 5, 5, 5])
		).front;
		wind.view.deleteOnClose = false; // win not destroyed

		// Restaurer devices depuis session
		defer {
			somax_obj.load_session;

			if(server.options.inDevice.notNil) {
				var inDev = server.options.inDevice.asString;
				var idx = dlI.detectIndex({ |item| item.asString == inDev });
				if(idx.notNil) { inDeviceMenu.value_(idx) };
			};
			if(server.options.outDevice.notNil) {
				var outDev = server.options.outDevice.asString;
				var idx = dlO.detectIndex({ |item| item.asString == outDev });
				if(idx.notNil) { outDeviceMenu.value_(idx) };
			};
		};
	}
	add_color_item {
		agentcolors.add(Color(0.40919312169312, 1, 0, 0.70634920634921));  // ListView colors
		agentview.colors_(agentcolors.asArray);
		"agentcolors".postln;
		agentcolors.postln;
	}
}
