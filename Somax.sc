Somax { // v. 2.70

	classvar <>server;
	var osc_to_server, <>players, <>players_info, players_recv_osc, <>agent,  agentcount = 0, <>somax_gui, <>player_gui_dico, <>player_output, buffers, <>soundfile, buf_agent, test_buid_buf, <>nodes, <>corpus_items, <>menu_corpus_items, recv_port = 1236, send_port = 1237, corpus_path, <>audio_influencer_dico, audio_influencer_buffer,<>influencer_gui_dico, <>influencerl, influencercount = 0, buf_onsets, buf_mfcc, stats_buf, point_buf, data_set, data_set_redux, data_set_stand, labels, labels_dico, audio_influencer_player, <>audio_influencer_info, audio_influencer_bus,  audio_descriptors, audio_descriptors_chroma, audio_descriptors_pitch, <>audio_descriptors_onset, audio_descriptors_mfcc, audio_group, <>audio_group_lev, vst_instrument, midi_instrument, displayonsets, index_list, midi_influencer_dico, midi_influencer_player, midi_influencer_events, midi_file_play, midipitchclass, midichroma, midi_influencer_dico, midi_functions, midi_out, ana_file_name, onset_limiter = 150, blinking, <>flucoma_plotter_dico, <>outputSelect, lastPitchTimes, lastAcceptedPitches, iemReverb, iemEQ, offset_out = 0, audio_to_decoder_bus, <>hoa_decoder = nil;
	// MVC
	var <>model_server, model_server_params, server_controllers;
	var <>preset_model, preset_params, preset_controllers, preset_path;
	// Osc control
	var osc_control; // OSC control dynamique
	// Session
	var session_path;

	// spatial
	var player_ambi_distribution;

	*new {
		^super.newCopyArgs().init();
	}


	init {
		/*var osc, buf_index, index = 0, num_seg;*/
		// objects dico
		"Init_Somax...".postln;
		agent = List[]; // for list order
		influencerl = List[]; // for list order
		players = Dictionary.new;
		players_info = Dictionary.new;
		players_recv_osc = Dictionary.new;
		player_gui_dico = Dictionary.new;
		player_output = Dictionary.new;
		buffers = Dictionary.new;
		soundfile = Dictionary.new;
		nodes = Dictionary.new;
		corpus_items = Dictionary.new;
		audio_influencer_dico = Dictionary.new;
		audio_influencer_buffer = Dictionary.new;
		audio_influencer_player = Dictionary.new;
		audio_influencer_info  = Dictionary.new;
		audio_influencer_bus = Dictionary.new;
		influencer_gui_dico = Dictionary.new;
		audio_descriptors = Dictionary.new;
		audio_descriptors_chroma  = Dictionary.new;
		audio_descriptors_pitch = Dictionary.new;
		audio_descriptors_onset = Dictionary.new;
		audio_descriptors_mfcc  = Dictionary.new;
		labels_dico = Dictionary.new;
		// audio_descriptors_synth = Dictionary.new;
		audio_group = Dictionary.new;
		audio_group_lev = Dictionary.new;
		vst_instrument = Dictionary.new;
		midi_instrument = Dictionary.new;
		midi_influencer_dico = Dictionary.new;
		midi_influencer_player = Dictionary.new;
		midi_influencer_events = Dictionary.new;
		midi_file_play = Dictionary.new;
		midipitchclass = Dictionary.new;
		midichroma = Dictionary.new;
		midi_influencer_dico = Dictionary.new;
		midi_functions = Dictionary.new;
		server = Server.default;
		index_list = [];
		blinking = Dictionary.new;

		// pitch filters
		lastPitchTimes = Dictionary.new;
		lastAcceptedPitches = Dictionary.new;

		flucoma_plotter_dico = Dictionary.new;

		iemReverb = Dictionary.new;
		iemEQ = Dictionary.new;

		player_ambi_distribution = Dictionary.new;

		server_controllers = Dictionary.new;
		model_server_params = ();

		model_server = (
			params: model_server_params,
			setParam: { |self, name, value|
				self[\params][name] = value;
				self.changed(\param, name, value);
				// self
			},
		);


		// ---- PRESET MODEL ----
		preset_path = Platform.userAppSupportDir +/+ "Somax/presets/";
		if(File.exists(preset_path).not) { File.mkdir(preset_path) };

		preset_params = ();
		preset_model = (
			params: preset_params,
			setParam: { |self, name, value|
				self[\params][name] = value;
				self.changed(\preset, name, value);
			},
		);

		session_path = Platform.userAppSupportDir +/+ "Somax/somax_config.json";
		this.load_session;

		// Controls MVC based on James Harkins code
		server_controllers.add(\somax_server_status -> SimpleController(model_server).put(\param, { |obj, what, key, value|
			if(somax_gui.notNil && key == \somax_server_status)
			{
				defer { somax_gui.server_button.value_(value); }; // send to gui
			}
		}));
		server_controllers.add(\somax_server_run -> SimpleController(model_server).put(\param, { |obj, what, key, value|
			if(somax_gui.notNil && key == \somax_server_run)
			{
				defer { somax_gui.runToggle.value_(value); }; // send to gui
				/*				if(value == 1)
				{
				this.run;
				}
				{
				this.stop;
				}*/

			}
		}));
		server_controllers.add(\corpus_path -> SimpleController(model_server).put(\param, { |obj, what, key, value|
			if(somax_gui.notNil && key == \corpus_path)
			{
				defer { somax_gui.path_string.string_("Corpus path :"++value); }; // send to gui
			}
		}));
		server_controllers.add(\create_agent -> SimpleController(model_server).put(\param, { |obj, what, key, value|
			if(somax_gui.notNil && key == \create_agent)
			{
				defer { somax_gui.agentview.items_(value);
					somax_gui.add_color_item;
					somax_gui.influencer_playerview.items_(value);
					/*somax_gui.agentview.selection_([]);*/
				}; // send to gui
			}
		}));
		server_controllers.add(\delete_agent -> SimpleController(model_server).put(\param, { |obj, what, key, value|
			if(somax_gui.notNil && key == \delete_agent)
			{
				defer { somax_gui.agentview.items_(value);
					somax_gui.influencer_playerview.items_(value);
				}; // send to gui
			}
		}));
		server_controllers.add(\audio_influencer -> SimpleController(model_server).put(\param, { |obj, what, key, value|
			if(somax_gui.notNil && key == \audio_influencer)
			{
				// value.postln;
				defer { somax_gui.influencerview.items_(value);
					// somax_gui.influencerview.selection_([]);
				}; // send to gui
			}
		}));
		/*		server_controllers.add(\audio_influencer_SF -> SimpleController(model_server).put(\param, { |obj, what, key, value|
		if(influencer_gui_dico[value[0]].notNil && key == \audio_influencer_SF)
		{
		// value.postln;
		"ERRRRROOOOOOOOR1111".postln;
		defer { influencer_gui_dico[value[0]].load_soundFile(value);
		// somax_gui.influencerview.selection_([]);
		}; // send to gui
		}
		}));*/
		server_controllers.add(\audio_influencer_play -> SimpleController(model_server).put(\param, { |obj, what, key, value|
			// if(influencer_gui_dico[value[0]].notNil && key == \audio_influencer_play)
			/*			"MVCCONTROL00000000".postln;
			value.postln;
			value.class.postln;
			influencer_gui_dico.postln;
			key.postln;
			"MVCCONTROL00000000".postln;*/
			if((influencer_gui_dico.size > 0 )&& (key == \audio_influencer_play))
			{
				if(influencer_gui_dico[value[0]].notNil )
				{
					// value.postln;
					/*					"MVCCONTROL".postln;
					value.postln;
					"MVCCONTROL".postln;
					"ERRRRROOOOOOOOR1111".postln;*/
					defer { influencer_gui_dico[value[0]].playButton.setPlaying(value[1]);
						// somax_gui.influencerview.selection_([]);
					}; // send to gui
				}
			}
		}));
		server_controllers.add(\audio_influencer_loop -> SimpleController(model_server).put(\param, { |obj, what, key, value|
			if((influencer_gui_dico.size > 0 )&& (key == \audio_influencer_loop))
			{
				if(influencer_gui_dico[value[0]].notNil )
				{
					defer { influencer_gui_dico[value[0]].loopButton.state_(value[1]);
						// somax_gui.influencerview.selection_([]);
					}; // send to gui
				}
			}
		}));


		// Controller : met à jour les menus preset dans toutes les GUIs player
		server_controllers.add(\preset_list -> SimpleController(preset_model).put(\preset, { |obj, what, key, value|
			if(key == \preset_list) {
				defer {
					player_gui_dico.do { |gui|
						gui.preset_menu.items_(["presets", "new", "replace"] ++ value);
					};
				};
			};
		}));
		// }));

		/*		MIDIClient.init;
		midi_out = MIDIOut(0);*/

		// launch Somax Python server
		// ["/Users/josephfernandez/Documents/Max 8/Packages/somax/misc/launch_local"].unixCmd;
		// ["/Users/josephfernandez/Documents/Max 8/Packages/Somax-2.6.1/misc/launch_local"].unixCmd;
		// ["python3 /Users/josephfernandez/Somax2/python/somax/somax_server.py"].unixCmd;
		this.load_synthdefs;
		this.descriptors_osc_replay;
		this.init_osc_control;

		// ^this;
	}

	gui {
		// ^super.newCopyArgs().init();
		// server = Server.default;
		somax_gui = Somax_gui.new(this);

		if(server.serverRunning)
		{
			somax_gui.server_status.value_(1)
		};
	}

	start {
		var osc, buf_index, index = 0, num_seg, python, script;

/*		python = Platform.userHomeDir +/+ "miniconda3/envs/somax3.9/bin/python3";
		// script = Platform.userHomeDir +/+ "Documents/Somax2/python/somax/somax_server.py";
		script = Platform.userHomeDir +/+ "Documents/GitHub/Somax2/python/somax/somax_server.py";
		(python + " " + script).unixCmd;*/

		"pkill -f somax_server".unixCmd; // kill OSC process

		// select platform to run python server
		Platform.case(
			\osx,       {
				// (Platform.userExtensionDir +/+ "SoCollider/misc/somax_server.app/Contents/MacOS/somax_server").unixCmd(true)
				(Platform.userHomeDir +/+ "Documents/Max\\ 9/Packages/Somax-2.7.0/misc/somax_server.app/Contents/MacOS/somax_server").unixCmd(true);
			},
			\linux,     { "Linux".postln },
			\windows,   { "Windows".postln }
		);

		osc_to_server = NetAddr("localhost", 1234); // adress to send from sclang to Pyton server

		// receiver from server
		OSCdef(\SoServer_receiver, {|msg|
			// ("server_raw :"++msg).postln;
			case
			{msg[1] == \beat}
			{
				if(msg[2] == \bang)
				{
					// Server running to button blink GUI
					if(somax_gui.notNil)
					{
						somax_gui.runButton.blink;
					}
				}
			}
			{msg[1] == \initialized}
			{
				osc_to_server.sendMsg('/somax', \set_tempo_master, \None);
				osc_to_server.sendMsg('/somax', \set_tempo, 120);
				osc_to_server.sendMsg('/somax', \stop_transport);
				// somax_server_status = 1;
				model_server.setParam(\somax_server_status, 1)
			}
			{msg[1] == \corpusbuilder}
			{
				// msg.postln;
				case
				{msg[2] == \stats}
				{
					index = 0;
					index_list = [];
					num_seg = msg[4];
					if(buf_onsets.notNil)
					{
						/*						buf_onsets.free;
						server.sync;*/
						buf_onsets = Buffer.alloc(server,msg[4]); // number of segements
						buf_mfcc = Buffer(server);
						stats_buf = Buffer(server);
						point_buf = Buffer(server);
						data_set = FluidDataSet(server);
						data_set_redux = FluidDataSet(server);
						data_set_stand = FluidDataSet(server);
						// data_se_norm = FluidDataSet(server);

						// labels = FluidLabelSet(server); // kmeans
					}
					{
						buf_onsets = Buffer.alloc(server,msg[4]); // number of segements
						buf_mfcc = Buffer(server);
						stats_buf = Buffer(server);
						point_buf = Buffer(server);
						data_set = FluidDataSet(server);
						data_set_redux = FluidDataSet(server);
						data_set_stand = FluidDataSet(server);
						// data_se_norm = FluidDataSet(server);
					};

				}
				{msg[2] == \seg}
				{
					/*					buf_onsets.set(index, msg[3]*48000); // sampling rate
					if(index == msg[4])
					{
					this.display_test_corpus;
					};*/
					index_list = index_list.add(msg[3]*48000);
					/*"index".postln;*/
					// index.postln;
					if(index == (num_seg -1))
					{
						// "send2buffer".postln;
						/*					if(displayonsets.notNil)
						{
						"refresh".postln;
						buf_onsets.sendCollection(index_list, action: {arg buf;
						{0.1.wait{displayonsets.addIndicesLayer(test_buid_buf, buf_onsets).refresh}.fork};
						})
						}
						{*/
						"buffers0".postln;
						buf_onsets.postln;
						buf_mfcc.postln;
						stats_buf.postln;
						point_buf.postln;
						data_set.postln;
						"buffers1".postln;
						buf_agent = Buffer.read(server, corpus_path++ana_file_name);
						buf_onsets.sendCollection(index_list, action: {arg buf;
							{0.1.wait;{this.display_test_corpus;}.defer}.fork;
							buf_onsets.write(corpus_path++ana_file_name.findRegexp("^(.*)\\.")[1][1]++"_slices.aif", "aiff", "float"); // record slices buffer to corpus folder
							{0.1.wait;{buf_onsets.plot;}.defer}.fork;

							buf_onsets.loadToFloatArray(action:{
								arg fa;
								fa.doAdjacentPairs{
									arg start, end, i;
									var num = end - start;
									"start".postln;
									start.postln;
									"num".postln;
									num.postln;
									/*									"toto".postln;
									buf_mfcc.postln;
									"toto1".postln;
									ana_file_name.postln;
									(corpus_path++ana_file_name).postln;*/

									/*									FluidBufMFCC.processBlocking(server, corpus_path++ana_file_name,start,num,features:buf_mfcc,numCoeffs:13,startCoeff:1,numChans:1);*/
									FluidBufMFCC.processBlocking(server,buf_agent,start,num,features:buf_mfcc,numCoeffs:13,startCoeff:1,numChans:1);

									FluidBufStats.processBlocking(server, buf_mfcc,stats:stats_buf);
									FluidBufFlatten.processBlocking(server,stats_buf,numFrames:1,destination:point_buf);

									data_set.addPoint("slice-%".format(i),point_buf);
									if(i % 500 == 1,{server.sync});
									"% / % done".format(i+1,buf_onsets.numFrames-1).postln;
								};
								buf_mfcc.write(corpus_path++ana_file_name.findRegexp("^(.*)\\.")[1][1]++"_mfcc.aif", "aiff", "float"); // record slices buffer to corpus folder
								data_set.write(corpus_path++ana_file_name.findRegexp("^(.*)\\.")[1][1]++"_data_set.json");
								// Reduce to 2 Dimensions
								FluidStandardize(server).fitTransform(data_set, data_set_stand);
								FluidUMAP(server,2).fitTransform(data_set_stand,data_set_redux,{"umap complete".postln});
								data_set_redux.write(corpus_path++ana_file_name.findRegexp("^(.*)\\.")[1][1]++"_data_set_redux.json");
								/*								FluidNormalize(server).fitTransform(data_set_stand, data_se_norm,{"normalize complete".postln});
								data_se_norm.write(corpus_path++ana_file_name.findRegexp("^(.*)\\.")[1][1]++"_data_set_norm.json");*/
								// kmeans on the dim redux dataset
								// FluidKMeans(server,3).fitPredict(data_set_redux, labels); // try with a different number of clusters

							});
						});
					};
					index = index + 1;
				}
			}
		}, '/somax', nil, 1235);

		osc_to_server.sendMsg('/somax', \start_transport);
	}

	run {
		osc_to_server.sendMsg('/somax', \start_transport);
		model_server.setParam(\somax_server_run, 1);
		audio_to_decoder_bus = Bus.audio(server, 25); // order 7  bus receiver Decoder; donde agentes envian
		// Synth(\HOADecSTUDIO5_7, [\in, audio_to_decoder_bus.index, \out, offset_out], addAction:'addToTail');
/*		Synth(\HOA_Dec_ESPRO75_Rec, [\in, audio_to_decoder_bus.index, \out, offset_out], addAction:'addToTail');*/
		// Synth(\HOA_Octo_Dec7, [\in, audio_to_decoder_bus.index, \out, offset_out], addAction:'addToTail'); // octo
	}

	stop {
		osc_to_server.sendMsg('/somax', \stop_transport);
		model_server.setParam(\somax_server_run, 0)
	}

	exit {
		vst_instrument.values.collect({|instr| // free all vst instr
			vst_instrument[instr.asSymbol].free;
		});
		osc_to_server.sendMsg('/somax', \exit);
		"pkill -f somax_server".unixCmd; // ← ajoute cette ligne
		OSCdef(\SoServer_receiver).free; // unregister OSC
		OSCdef(\somax_control).free;
		OSCdef.freeAll; // unregister all OSC
		audio_group.values.collect({|group| // free all nodes
			server.sendMsg(\g_freeAll, group.nodeID);
		});
		if(somax_gui.notNil)
		{
			model_server.setParam(\somax_server_status, 0);
		};

		{0.1.wait;
			{audio_group.values.collect({|group| // free all groups
				group.nodeID.postln;
				server.sendMsg(\n_free, group.nodeID);
			});
		}.defer}.fork;
	}

	corpus_path {|path|
		corpus_path = path;
		model_server.setParam(\corpus_path, corpus_path)
	}

	select_corpus_path {
		FileDialog({ |paths|
			postln("Selected paths:"++paths[0]);
			corpus_path = paths[0];
			model_server.setParam(\corpus_path, corpus_path)
		}, fileMode: 2);
	}

	create_agent {|player, corpus = nil, instr = nil, channels = nil, speakers = 0, offset_out = 0, ambisonic = nil|
		var so_index, cluster_lab, audio_out_ch, segments = 0, onset_node, audio_ready = false;

		if (players.includesKey(player.asSymbol).not)
		{
			this.player_gui(player.asSymbol); // create new gui

			players_recv_osc.add(player.asSymbol -> NetAddr("localhost", recv_port)); // adress to send from sclang to Pyton player

			players_info.add(player.asSymbol -> [corpus, speakers, segments, onset_node, audio_ready]);
			players.add(player.asSymbol -> List[\agent]); // init no type
			agent.insert(agentcount, player.asSymbol -> 0);
			player_output.add(player.asSymbol -> "ambisonic"); //default ambisonic output
			"agent_insert_parent".postln;
			agent.postln;
			"agent_iagentcount".postln;
			agentcount.postln;

			/*		"PLAYERS".postln;
			players.includesKey(player.asSymbol).not
			players.postln;
			players.add(player.asSymbol -> List[\agent]); // init no type
			"PLAYERS".postln;

			if(agent.size == 0)
			{
			agent.insert(agentcount, player.asSymbol -> 0);
			}{
			agent.do({|item|
			if(item.key != player.asSymbol)
			{
			/*					"DISTINTO".postln;
			item.postln;
			"DISTINTO".postln;*/
			}
			{
			/*					"IGUAL".postln;
			item.postln;
			"IGUAL".postln;*/
			}
			});
			agent.insert(agentcount, player.asSymbol -> 0);
			};*/

			player_ambi_distribution.add(player.asSymbol -> \random); // default
			audio_out_ch = 16; // maximum number of channel per Agent (it's depend on ambisonic order)

			/*			if(speakers !=0)
			{
			audio_out_ch = 14 //speakers.flat.maxItem; arranger
			}
			{
			audio_out_ch = 2
			};*/

			// from server player
			OSCdef(player.asSymbol, {|msg, time, addr, recvPort|
				var out_nodeID, onset_descriptor, win_visible;
				/*"TTTTTTTTTTTTTT".postln;
				("player_raw :"++msg).postln;
				players_recv_osc.postln;
				recvPort.postln;
				// players.postln;
				// recv_port.postln;
				"TTTTTTTTTTTTTT".postln;*/

				// ("player_raw :"++msg).postln;

				case
				{msg[1] == \state}
				{
					so_index = msg[2]; // segment index
					// ("index :"++so_index).postln;
					defer {
						win_visible = player_gui_dico[player].win.view.visible;
						if(win_visible) // if player GUI active
						{
							player_gui_dico[player].state(so_index); // send state to state_slider
						}
					};
				}
				{msg[1] == \output_type}
				{
					defer {
						win_visible = player_gui_dico[player].win.view.visible;
						if(win_visible) // if player GUI active
						{
							defer {player_gui_dico[player].match(msg[2]);}
						}
					};
					/*"output_type".postln;
					msg.postln;*/
					/*					if(player_gui_dico[player].win.view.visible) // if player GUI active
					{
					// defer {player_gui_dico[player].match(msg[2]);}
					}*/
					/*					match
					no_match
					fallback*/
					/*					so_index = msg[2]; // segment index
					// ("index :"++so_index).postln;
					if(player_gui_dico[player].win.view.visible) // if player GUI active
					{
					player_gui_dico[player].state(so_index); // send state to state_slider
					}*/

				}
				{msg[2] == \event}
				{
					case
					{(msg[1] == \audio) && ((player_output[player.asSymbol] == "mono") || (player_output[player.asSymbol] == "stereo"))} // channels == nil)}
					{
						/*"normal".post;
						msg.postln;*/
						this.play_slice(player, msg[3], msg[4], msg[5]);
					}
					{(msg[1] == \audio) && (player_output[player.asSymbol] == "multichannel")} //channels == 24}
					{
						this.play_slice24(player, msg[3], msg[4], msg[5]);
					}
					{(msg[1] == \audio) && (player_output[player.asSymbol] == "ambisonic")}
					{
						var pos = this.ambi_position(player_ambi_distribution[player.asSymbol] ? \random);
						this.play_slice_ambi(player, msg[3], msg[4], msg[5], pos[0], pos[1], pos[2]);
					}
					{(msg[1] == \audio) && (player_output[player.asSymbol] == "aoo")} //channels == "aoo"}
					{
						var labels, win_visible;
						// "test1".postln;
						// ("start_time :"++msg[3]).postln;
						// so_index = ~dicoTest.at((msg[3]*0.001*48000).round);
						// ("so_index :"++so_index).postln;
						// flucoma_plotter_dico[player].labels.getLabel(("slice-"++so_index).asString, {|lab| cluster_lab = lab; });
						labels = labels_dico[player.asSymbol]; // retrieve FluidLabelSet

						labels.getLabel(("slice-"++so_index).asString, {|lab| cluster_lab = lab; });
						// ~labels.getLabel(("slice-"++so_index).asString, {|lab| cluster_lab = lab; });
						// ("cluster_lab :"++cluster_lab).postln;

						this.play_slice_aoo(player, msg[3], msg[4], msg[5], cluster_lab.asInteger);

						defer {
							win_visible = flucoma_plotter_dico[player].win.view.visible;
							if(win_visible) // if plotter GUI active
							{
								flucoma_plotter_dico[player].plotter.highlight_("slice-"++so_index); // send to FluidPlotter
							}
						};
						// ~fp.highlight_("slice-"++so_index); // send to FluidPlotter
					}
					{(msg[1] == \audio) && channels == "aoo2"}
					{
						// ~labels.getLabel(("slice-"++so_index).asString, {|lab| cluster_lab = lab; });
						var speak ;
						speak = players_info[player.asSymbol][1];
						if(speak.isArray)
						{
							this.play_slice_aoo2(player, msg[3], msg[4], msg[5], speak, cluster_lab = 0); // clustering
						}{
							this.play_slice_aoo2(player, msg[3], msg[4], msg[5], 0, 0); // No clustering
						};
						// ~fp.highlight_("slice-"++so_index); // send to FluidPlotter
					}
					{msg[1] == \midi}
					{
						// "MIDI_MESSAGE".postln;
						// msg.postln;
						this.play_midi(player, msg[3..5]);
						/*					if(midi_influencer_dico.includesKey(player)) // if midi influencer
						{

						}
						{
						this.play_midi(player, msg[3..5]);
						}		*/
					}
				}
				{(msg[2] == \audio_off) && (players.includesKey(player)) } // &&(players[player.asSymbol] == \audio)
				{
					var ready ;
					ready = players_info[player.asSymbol][4];

					if(ready) //(players_info[player][0].notNil) // If corpus was loaded
					{
						// ("player_raw :"++msg).postln;
						case
						{(player_output[player.asSymbol] == "mono") || (player_output[player.asSymbol] == "stereo")}
						{ this.play_slice(player, "off")}
						{player_output[player.asSymbol] == "multichannel"}
						{ this.play_slice24(player, "off") }
						{player_output[player.asSymbol] == "ambisonic"}
						{ this.play_slice_ambi(player, "off") }
						{player_output[player.asSymbol] == "aoo"}
						{ this.play_slice_aoo(player, "off") }
						{player_output[player.asSymbol] == "aoo2"}
						{ this.play_slice_aoo2(player, "off") }
						// msg.postln;
					}
				}
				{msg[1] == \initialized}
				{
					// "INITIALIZEINITIALIZEINITIALIZEINITIALIZEINITIALIZE".postln;
					players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \get_corpus_files, corpus_path);
					players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \jump, 0.0);
					this.init_agent(player);
				}
				{msg[1] == \position}
				{
					var sr = 48000; // Sampling rate
					var start = (msg[2]*0.001)*sr;
					var end = (msg[3]*0.001)*sr;
					var dur = end-start;
					/*					"POSITION".postln;
					msg.postln;*/
					defer {
						player_gui_dico[player].soundFile.setSelectionStart(0, start); // send pos to SoundFileView
						player_gui_dico[player].soundFile.setSelectionSize(0, dur);
					};
				}
				{msg[1] == \corpusview} // reading_corpus_status, init failed success
				{

					/*("player_raw :"++msg).postln;*/
					case
					{msg[2] == \reading_corpus_status} //
					{
						"reading_corpus_status_2".postln;

						/*						if(player_gui_dico[player].win.view.visible) // if player GUI active
						{*/
						case
						{msg[3] == \init} //
						{
							"Init".postln;
							player_gui_dico[player].loading;
						}
						{msg[3] == \failed} //
						{
							"failed".postln;
						}
						{msg[3] == \success} //
						{
							"success_gui".postln;
							player_gui_dico[player].blinking.stop;
							players_info[player.asSymbol][4] = true;
						}
						// }
						{
							case
							{msg[3] == \success}
							{
								"success_code".postln;
								// audio_ready = true;
								players_info[player.asSymbol][4] = true;
								"audio_ready".postln;
								(players_info[player.asSymbol][4]).postln;
							}
						}
					}
					{msg[2] == \loaded_corpus}
					{
						"load_corpus_buffer".postln;
						msg.postln;
						players_info[player.asSymbol].postln;
						players_info[player.asSymbol][2] = msg[5];
						segments = msg[5];

						"load_corpus_buffer".postln;
						case
						{msg[4] == \AudioCorpus} // load audio buffer
						{
							/*"TTTTTTEEEEEEEEESSSSSSTTTTT".postln;
							/*							player_gui_dico.postln;
							players.postln;*/
							msg.postln;
							"TTTTTTEEEEEEEEESSSSSSTTTTT".postln;*/
							if(players[player][0] == \audio) // && player_gui_dico[player].win.view.visible)
							{
								// "TTTTTTEEEEEEEEESSSSSSTTTTT000".postln;
								out_nodeID = audio_group_lev[player].nodeID;
								onset_descriptor = audio_descriptors_onset[player.asSymbol];
								this.player_gui_sf(player.asSymbol, msg[6], out_nodeID, msg[5], onset_descriptor); // send path to GUI
								this.load_audio(player, msg[6]);
								/*if(channels == 24)
								{
								this.load_audio24(player, msg[6]);
								}{
								this.load_audio(player, msg[6]);
								};*/
							}
							{ // create new
								// "TTTTTTEEEEEEEEESSSSSSTTTTT111".postln;

								// this.player_gui(player.asSymbol); // create new gui
								"orden_3".postln;
								// {1.0.wait;{ //
								"player_GUI_dico".postln;
								player_gui_dico.postln;
								audio_group.add(player.asSymbol -> Group.new);
								nodes.add(player.asSymbol -> [nil, 0]); // init nodes_id
								// players.add(player.asSymbol -> [\audio]); // init
								players[player][0] = \audio;
								audio_influencer_bus.add(player.asSymbol -> Bus.audio(server, audio_out_ch)); // Audio bus

/*								audio_group_lev.add(player.asSymbol -> Synth(\HOA_Out_7, [\in, audio_influencer_bus[player.asSymbol].index, \globTBus, audio_to_decoder_bus.index], audio_group[player.asSymbol], 'addToTail'));*/

								audio_group_lev.add(player.asSymbol -> Synth(\audioOut++audio_out_ch, [\in, audio_influencer_bus[player.asSymbol].index, \out, offset_out], audio_group[player.asSymbol], 'addToTail'));

/*								audio_group_lev.add(player.asSymbol -> Synth(\HOA_Dec_ESPRO75_Rec, [\in, audio_influencer_bus[player.asSymbol].index, \out, offset_out], audio_group[player.asSymbol], 'addToTail'));*/

								out_nodeID = audio_group_lev[player].nodeID;
								out_nodeID.postln;

								//// prepare player as influencer
								audio_influencer_info.add(player.asSymbol -> [0.15, nil, \playerInfluencer, true, 0.75, "Onset"]); // store influencer data onset_limiter, etc 0.75-> pitch quality
								// audio_influencer_info.postln;
								/*lastPitchTimes.add(player.asSymbol -> nil); // for pitch filter
								lastAcceptedPitches.add(player.asSymbol -> 0); // for pitch filter*/

								this.audio_descriptors(player.asSymbol); // instantiate descriptors

								// {0.01.wait;{ //
								onset_descriptor = audio_descriptors_onset[player.asSymbol];
								// "TTTTTTEEEEEEEEESSSSSSTTTTT222".postln;
								// if(player_gui_dico[player].win.view.visible) // if player GUI active
								// {
								this.player_gui_sf(player.asSymbol, msg[6], out_nodeID, msg[5], onset_descriptor); // send path to GUI
								// };
								// }.defer}.fork;
								/*server.sync;
								out_nodeID = audio_group_lev[player].nodeID;
								"NODE_ID".postln;
								out_nodeID.postln;
								"NODE_ID".postln;*/

								if(channels == 24)
								{
									this.load_audio24(player, msg[6]);
								}{
									this.load_audio(player, msg[6]);
								};
								// }.defer}.fork;

							} //

						}
						{msg[4] == \MidiCorpus} // load midi VST instrument
						{
							if(instr.notNil)
							{
								// players.add(player.asSymbol -> [\midi]); // init
								players[player][0] = \midi;
								midipitchclass.add(player.asSymbol ->  ComputeMemoryPitchClass.new); // midi chroma
								this.load_midi_instrument(player.asSymbol, instr);
							}
							{
								"you need to load a VST instrument".postln;
							}
						}
					}
					{msg[2] == \corpus_info}
					{
						// ("player_raw :"++msg).postln;
						if(msg[3] == \bang) // bang when finish
						{
							menu_corpus_items = corpus_items.values.collect({|mess|
								mess[0];
							});
							menu_corpus_items = menu_corpus_items.sort.addFirst("refresh corpus");
							player_gui_dico[player.asSymbol].corpus_menu_items(menu_corpus_items); // send menu to player_gui
							if(corpus.isString)
							{
								"load_corpus0000".postln;
								// var a_ready = players_info[player.asSymbol][4];
								this.load_corpus(player, corpus, speakers, segments, onset_node, false);
								// this.setmenu(player, menu_corpus_items.indexOfEqual(corpus));
								// menu_corpus_items.postln
							} {
								("load corpus in "+player.asSymbol).postln
							};
						}
						{
							// ("corpus_infos " + msg).postln;
							corpus_items.add(msg[3].asString.drop(4) -> [msg[3], msg[4]]);
							// corpus_items.values;
						};
						/*						if(player_gui_dico[player].win.view.visible)
						{
						this.setmenu(player, menu_corpus_items.indexOfEqual(corpus));
						}*/

					}
				}
			}, player.asSymbol, nil, send_port);

			osc_to_server.sendMsg('/somax', \create_agent, 'name=', player.asSymbol,  'recv_port=', recv_port, 'send_port=', send_port, 'ip=', '127.0.0.1', 'override=True');

			// receive corpus names
			recv_port = recv_port + 2; // For next receive osc port
			send_port = send_port + 2; // For next send osc port

			model_server.setParam(\create_agent, agent.collect({|x| x.key}).reverse); //send agent list ordered, MVC
			/*			"agents list".postln;
			agent.postln;
			agent.collect({|x| x.key}).postln;*/
		}
		{
			"Agent already created".postln;
		}
	}

	corpus {
		corpus_items.values.postln;
		corpus_items.keys.postln;
	}

	init_agent {|player|
		// v.2.70
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \add_filter, 'NextStateFilter', 'verbose=False');
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \add_filter, 'BinaryTransformContinuityFilter', 'verbose=False');
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \add_filter, 'ThresholdFilter', 'verbose=False');
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \add_filter, 'StaticTabooFilter', 'verbose=False');
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \render_features, 'pitch', 'chroma');
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \create_atom, 'self');
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \create_atom, 'melodic');
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \create_atom, 'harmonic');
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \create_atom, 'selfharmonic');
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \create_atom, 'mfcc');
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \create_atom, 'selfmfcc');
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'NextStateFilter::_factor', 1.5);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_scheduling_handler, 'AutomaticSchedulingHandler');
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_scheduling_handler, 'ManualSchedulingHandler');
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'ThresholdFilter::enabled', 0);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'mfcc::_weight', 0.5);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'harmonic::_weight', 0.5);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'melodic::_weight', 1.0);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'selfmfcc::_weight', 0.25);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'selfharmonic::_weight', 0.25);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'self::_weight', 0.25);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \enabled, \True);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \remove_transform, 'TransposeTransform', 'semitones=', -5);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \remove_transform, 'TransposeTransform', 'semitones=', -4);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \remove_transform, 'TransposeTransform', 'semitones=', -3);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \remove_transform, 'TransposeTransform', 'semitones=', -2);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \remove_transform, 'TransposeTransform', 'semitones=', -1);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \add_transform, 'TransposeTransform', 'semitones=', 0);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \remove_transform, 'TransposeTransform', 'semitones=', 1);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \remove_transform, 'TransposeTransform', 'semitones=', 2);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \remove_transform, 'TransposeTransform', 'semitones=', 3);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \remove_transform, 'TransposeTransform', 'semitones=', 4);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \remove_transform, 'TransposeTransform', 'semitones=', 5);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \remove_transform, 'TransposeTransform', 'semitones=', 6);

		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'BinaryTransformContinuityFilter::_factor', 1.0);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \remove_filter, 'VerticalDensityFilter', 'verbose=', \False);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \remove_filter, 'DurationFilter', 'verbose=', \False);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \remove_filter, 'OctaveBandsFilter', 'verbose=', \False);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \remove_filter, 'AutoJumpFilter', 'verbose=', \False);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \remove_filter, 'TempoConsistencyFilter', 'verbose=', \False);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \remove_filter, 'EnergyFilter', 'verbose=', \False);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_time_stretch, 1.0);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_timeout, \None);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_timeout, 2.0);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_artificial_ties, \False);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_peak_selector, 'MaxPeakSelector', 'verbose=', \False);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'fallback_selector::enforce_taboo', \True);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'fallback_selector::default_to_first', \False);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'post_filter::enabled', 0);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_recombine, \True);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'fallback_selector::output_if_none', \True);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_note_by_note_mode, \True);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_align_note_ons, \False);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_align_note_offs, 'Sustained');
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'StaticTabooFilter::_taboo_length', 0);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \add_filter, 'BeatPhaseFilter', 'override=', \True, 'verbose=', 0);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'BeatPhaseFilter::_align_to_clock', 0);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'BeatPhaseFilter::_scale_factor', 0.5);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'BeatPhaseFilter::_enforce_beat', 1);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'BeatPhaseFilter::_round_beat', 0);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'BeatPhaseFilter::_always_allow_next', 0);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'BeatPhaseFilter::_grid_size', 8);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'BeatPhaseFilter::enabled', 0);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'BeatPhaseFilter::_align_to_clock', 0);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_time_stretch, 1.0);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_synchronize_to_global_tempo, \false);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_decay_basis, 'event');
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_timeout_release, 'None');
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'self::_activity_pattern::tau_mem_decay', 2.000999927520752);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'self::_memory_space::_ngram_size', 2);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'self::_weight', 0.25);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'self::_self_influenced', 1);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'self::_enabled', 1);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_classifier, 'atom_path=', 'self', 'classifier_name=', 'default', 'descriptor=', 'pitch', 'descriptor_is_label=', 0);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'melodic::_activity_pattern::tau_mem_decay', 2.000999927520752);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'melodic::_memory_space::_ngram_size', 2);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'melodic::_weight', 1.0);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'melodic::_self_influenced', 0);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'melodic::_enabled', 1);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_classifier, 'atom_path=', 'melodic', 'classifier_name=', 'default', 'descriptor=', 'pitch', 'descriptor_is_label=', 0);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'harmonic::_activity_pattern::tau_mem_decay', 3.000999927520752);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'harmonic::_memory_space::_ngram_size', 2);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'harmonic::_weight', 0.5);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'harmonic::_self_influenced', 0);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'harmonic::_enabled', 1);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_classifier, 'atom_path=', 'harmonic', 'classifier_name=', 'default', 'descriptor=', 'chroma', 'descriptor_is_label=', 0);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'selfharmonic::_activity_pattern::tau_mem_decay', 3.000999927520752);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'selfharmonic::_memory_space::_ngram_size', 2);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'selfharmonic::_weight', 0.25);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'selfharmonic::_self_influenced', 1);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'selfharmonic::_enabled', 1);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_classifier, 'atom_path=', 'selfharmonic', 'classifier_name=', 'default', 'descriptor=', 'chroma', 'descriptor_is_label=', 0);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'mfcc::_activity_pattern::tau_mem_decay', 3.000999927520752);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'mfcc::_memory_space::_ngram_size', 2);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'mfcc::_weight', 0.5);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'mfcc::_self_influenced', 0);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'mfcc::_enabled', 1);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_classifier, 'atom_path=', 'mfcc', 'classifier_name=', 'default', 'descriptor=', 'mfcc', 'descriptor_is_label=', 0);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'mfcc::_classifier::d_max', 5.0);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \get_param, 'mfcc::_classifier::_num_classes_param', \True);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'selfmfcc::_activity_pattern::tau_mem_decay', 3.000999927520752);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'selfmfcc::_memory_space::_ngram_size', 2);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'selfmfcc::_weight', 0.25);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'selfmfcc::_self_influenced', 1);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'selfmfcc::_enabled', 1);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_classifier, 'atom_path=', 'selfmfcc', 'classifier_name=', 'default', 'descriptor=', 'mfcc', 'descriptor_is_label=', 0);
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'selfmfcc::_classifier::d_max', 5.0);
	}

	load_corpus {|player, corpus, speakers, segments, onset_node, audio_ready|
		"corpus audio_ready".postln;
		audio_ready.postln;

		// Vérifier que le corpus existe dans corpus_items
		if(corpus_items[corpus].isNil) {
			("load_corpus: corpus not found in corpus_items -> " ++ corpus).postln;
			("Available corpus: " ++ corpus_items.keys).postln;
			^nil
		};

		corpus_items[corpus].postln;
		"corpus Type".postln;
		(corpus_items[corpus][0].asString.keep(3)).postln;
		(corpus_items[corpus][0].asString.keep(3)).class.postln;
		(corpus_items[corpus][0].asString.keep(3)).size.postln;
		"corpus Type".postln;

		if(corpus_items[corpus][0].asString.keep(3) == "(A)") // if Audio corpus
		{
			"1_load audio corpus".postln;
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \read_corpus, corpus++".pickle", 'corpuspath_folder=', corpus_path);
			"menu1".postln;
			menu_corpus_items.indexOf(("(A) "++corpus).asSymbol).postln;
			menu_corpus_items.postln;
			"menu2 ".postln;
			this.setmenu(player, menu_corpus_items.indexOf(("(A) "++corpus).asSymbol), corpus);
		}
		{ // else Midi corpus
			"load Midi corpus".postln;
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \read_corpus, corpus++".gz", 'corpuspath_folder=', corpus_path);
			if(player_gui_dico[player].win.view.visible)
			{
				this.setmenu(player, menu_corpus_items.indexOf(("(M) "++corpus).asSymbol));
			}
		}
	}

	load_audio {|player, corpus|
		if(buffers.includesKey(player.asSymbol))  // If already loaded
		{
			"Buffer_already".postln;
			buffers[player.asSymbol].free;
			buffers.add(player.asSymbol -> Buffer.read(server, corpus.asString));
		}
		{
			"Buffer_first".postln;
			buffers.add(player.asSymbol -> Buffer.read(server, corpus.asString));
		}
		// soundfile.add(player.asSymbol -> SoundFile.openRead(corpus.asString)); // for SoundFileView
	}

	load_audio24 {|player, corpus|
		"loadloadloadloadload".postln;
		(corpus.asString++"_24").postln;
		buffers.add(player.asSymbol -> Buffer.read(server, corpus.asString.findRegexp("^(.*)\\.")[1][1]++"_24.aif"));
	}
	playing_mode {|player, on_off|
		case
		{on_off == 1} {
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, 'set_scheduling_handler', 'ManualSchedulingHandler');
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, 'set_decay_basis', 'event');
			defer { player_gui_dico[player].playing_mode_Button.value_(1) }; // send to GUI
		}
		{on_off == 0} {
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, 'set_scheduling_handler', 'AutomaticSchedulingHandler');
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, 'set_decay_basis', 'time');
			defer { player_gui_dico[player].playing_mode_Button.value_(0) }; // send to GUI
		}
	}

	set_time_out {|player, time_out = 2.0|
		("set_time_out: " ++ player ++ " " ++ time_out ++ " " ++ time_out.class).postln;
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_timeout, time_out)
	}

	set_classifier_self {|player, on_off|
		case
		{on_off == 1} {
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_classifier, 'atom_path=', 'self', 'classifier_name=', 'default', 'descriptor=', 'pitchclass', 'descriptor_is_label=', 0)
		}
		{on_off == 0} {
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_classifier, 'atom_path=', 'self', 'classifier_name=', 'default', 'descriptor=', 'pitch', 'descriptor_is_label=', 0)
		}
	}
	set_classifier_melodic {|player, on_off|
		case
		{on_off == 1} {
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_classifier, 'atom_path=', 'melodic', 'classifier_name=', 'default', 'descriptor=', 'pitchclass', 'descriptor_is_label=', 0)
		}
		{on_off == 0} {
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_classifier, 'atom_path=', 'melodic', 'classifier_name=', 'default', 'descriptor=', 'pitch', 'descriptor_is_label=', 0)
		}
	}

	sparse {|player, on_off|
		case
		{on_off == 1} {players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'fallback_selector::output_if_none', \True)}
		{on_off == 0} {players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'fallback_selector::output_if_none', \False)}
	}

	time_stretch_on_off {|player, on_off|
		case
		{on_off == 1} {
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'BeatPhaseFilter::_align_to_clock', 1);
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_time_stretch, 1.0);
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_synchronize_to_global_tempo, \true)
		}
		{on_off == 0} {
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'BeatPhaseFilter::_align_to_clock', 0);
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_time_stretch, 1.0);
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_synchronize_to_global_tempo, \false)

		}
	}

	set_time_stretch {|player, val|
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_time_stretch, val)
	}

	add_transform {|player, val|
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \add_transform, \TransposeTransform, 'semitones=', val)
	}

	remove_transform {|player, val|
		players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \remove_transform, \TransposeTransform, 'semitones=', val)
	}

	ngram_size {|player, param, val|
		param.postln;
		val.postln;
		val.class.postln;
		case
		{param == \self }
		{
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'self::_memory_space::_ngram_size', val.asInteger)
		}
		{param == \selfharmonic }
		{
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'selfharmonic::_memory_space::_ngram_size', val.asInteger)
		}
		{param == \selfmfcc }
		{
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'selfmfcc::_memory_space::_ngram_size', val.asInteger)
		}
		// external
		{param == \melodic }
		{
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'melodic::_memory_space::_ngram_size', val.asInteger)
		}
		{param == \harmonic }
		{
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'harmonic::_memory_space::_ngram_size', val.asInteger)
		}
		{param == \mfcc }
		{
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'mfcc::_memory_space::_ngram_size', val.asInteger)
		}
	}

	// Midi commands
	note_by_note {|player, on_off|
		case
		{on_off == 1} {players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_note_by_note_mode, \True)}
		{on_off == 0} {players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_note_by_note_mode, \False)}
	}

	decay_basis {|player, on_off|
		case
		{on_off == \event} {players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_activity_pattern, \selfharmonic, \ManualActivityPattern);
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_activity_pattern, \harmonic, \ManualActivityPattern);
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_activity_pattern, \melodic, \ManualActivityPattern);
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_activity_pattern, \self, \ManualActivityPattern);
		}
		{on_off == \time} {players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_activity_pattern, \selfharmonic, \ClassicActivityPattern);
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_activity_pattern, \harmonic, \ClassicActivityPattern);
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_activity_pattern, \melodic, \ClassicActivityPattern);
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_activity_pattern, \self, \ClassicActivityPattern);
		}
	}

	cut_midi_event {|player, on_off|
		case
		{on_off ==  \allowed} {players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_scheduling_handler, \ManualSchedulingHandler)}
		{on_off == \not_allowed} {players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_scheduling_handler, \IndirectSchedulingHandler)}
	}

	set_params {|player, param, val|
		case
		{param == \sparse } {
			if(val == 1)
			{
				players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'fallback_selector::output_if_none', \False)
			}
			{
				players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'fallback_selector::output_if_none', \True)
			}
		}
		{param == \cut } {
			if(val == 1)
			{
				players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_scheduling_handler, \ManualSchedulingHandler)
			}
			{
				players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_scheduling_handler, \IndirectSchedulingHandler)
			}
		}
		{param == \continuity } {
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'NextStateFilter::_factor', val)} // 0 - 10
		{param == \quality } {
			if(val == 0)
			{
				players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'ThresholdFilter::enabled', 0); // OFF
			}
			{
				players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'ThresholdFilter::enabled', 1);
				players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'ThresholdFilter::_threshold', val)
			}
		}

		{param == \outputprobability } {
			if(val == 10.0)
			{
				players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'post_filter::enabled', 0);
			}
			{
				players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'post_filter::enabled', 1);
				players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'post_filter::output_probability', val)} // 0 - 1
		}
		{param == \enabled } {
			if(val == 1)
			{
				"Agent_Enable".postln;
				players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \enabled, 'True');
				player_gui_dico[player].enable_Button.value_(1); // send to GUI
			}
			{
				"Agent_Disable".postln;
				players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \enabled, 'False');
				player_gui_dico[player].enable_Button.value_(0); // send to GUI
			}
		}
		{param == \weights } {
			// internat
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'self::_weight', val[0]);
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'selfharmonic::_weight', val[1]);
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'selfmfcc::_weight', val[2]);
			// external
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'melodic::_weight', val[3]);
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'harmonic::_weight', val[4]);
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'mfcc::_weight', val[5]);
		}
		{param == \beat_align } {
			if(val == 1)
			{
				players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'fallback_selector::output_if_none', \True)
			}
			{
				players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_param, 'fallback_selector::output_if_none', \False)
			}
		}
		{param == \jump } {
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \jump, val.asInteger);
		}
		{true} {
			players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, param, val)
		}
	}

	// ---- PRESET : collecter tous les params d'un player ----
	collect_player_params { |player|
		var gui = player_gui_dico[player.asSymbol];
		var p = ();
		var m;
		var transpStates;

		if(gui.isNil) { ^nil };

		// --- Output ---
		p[\output_mode]             = gui.currentModeIndex;
		p[\output_mode_name] = gui.outputMenu.items[gui.currentModeIndex].asString; // "stereo", "ambisonic", etc.

		p[\amp]                     = gui.currentVal;
		p[\offset_out_mono]         = gui.offset_out_mono;
		p[\offset_out_stereo0]      = gui.offset_out_stereo0;
		p[\offset_out_stereo1]      = gui.offset_out_stereo1;
		p[\offset_out_multichannel] = gui.offset_out_multichannel;
		p[\offset_out_ambisonic]    = gui.offset_out_ambisonic;
		p[\offset_out_aoo]          = gui.offset_out_aoo;

		// --- Player state ---
		p[\enabled]                 = gui.enable_Button.value;
		p[\playing_mode]            = gui.playing_mode_Button.value;
		// Sauvegarder le nom du corpus au lieu de l'index
		if(gui.corpus_menu.value > 0 && gui.corpus_menu.items.notNil) {
			p[\corpus] = gui.corpus_menu.items[gui.corpus_menu.value].asString.drop(4); // enlève "(A) " ou "(M) "
		} {
			p[\corpus] = nil;
		};

		// --- Sliders ---
		p[\weights]                 = gui.weight_multislider.values.asArray;
		p[\continuity]              = gui.continuitySlider.value;
		p[\quality]                 = gui.qualitySlider.value;
		p[\probability]             = gui.probSlider.value;
		p[\time_stretch]            = gui.time_stretch_slider.value;

		// --- Toggles ---
		p[\cut]                     = gui.cutButton.value;
		p[\sparse]                  = gui.sparseButton.value;
		p[\timeout_toggle]          = gui.timeoutToggle.value;
		p[\time_stretch_toggle]     = gui.timeStretchToggle.value;
		p[\beat_align]              = gui.beatAlignButton.value;

		// --- Timeout ---
		p[\timeout_val]             = gui.timeoutVal;

		// --- Outgoing Influences ---
		p[\outgoing_influence]      = gui.outgoingInfluenceMenu.value;
		p[\onset_threshold]         = gui.onsetThresholdSlider.value;
		p[\onset_limiter]           = gui.onsetLimiterSlider.value;
		p[\pitch_quality]           = gui.pitchQualitySlider.value;

		// --- Transpositions [-5,-4,-3,-2,-1, 1,2,3,4,5,6] (sans le 0 fixe) ---
		if(gui.transpStates.notNil) {
			p[\transpositions] = gui.transpStates.asArray;
		};

		// --- MVC params ---
		m = gui.player_mvc_model_dico[player.asSymbol];
		if(m.notNil) {
			p[\mvc_params] = m[\params].asAssociations.collect({ |a|
				[a.key.asString, a.value]
			});
		};

		^p
	}

	// ---- PRESET : sauvegarder dans un fichier JSON ----
	save_preset { |preset_name, player|
		var json_path = preset_path ++ preset_name ++ ".json";
		var json_str;
		var p = this.collect_player_params(player);

		if(p.isNil) { ^nil };

		json_str = p.asJSON;
		File.use(json_path, "w", { |f| f.write(json_str) });

		("Preset saved: " ++ json_path).postln;
		preset_model.setParam(\preset_saved, preset_name);
		preset_model.setParam(\preset_list, this.list_presets);
	}

	// ---- PRESET : charger depuis un fichier JSON ----
	load_preset { |preset_name, player|
		var json_path = preset_path ++ preset_name ++ ".json";
		var json_str, p;
		var gui = player_gui_dico[player];
		var semitones = [-5, -4, -3, -2, -1, 1, 2, 3, 4, 5, 6];

		if(File.exists(json_path).not) {
			("Preset not found: " ++ json_path).postln;
			^nil
		};

		if(gui.isNil) { ^nil };

		json_str = File.use(json_path, "r", { |f| f.readAllString });
		p = json_str.parseJSON;

		// --- Output mode ---
		if(p["output_mode_name"].notNil) {
			var modeName = p["output_mode_name"].asString;
			var idx = gui.outputMenu.items.detectIndex({ |item|
				item.asString == modeName  // ← asString sur les deux pour comparer
			});
			if(idx.notNil) {
				defer {
					gui.currentModeIndex = idx;
					gui.outputMenu.value_(idx);
					gui.setupMode.(idx);
					if(audio_group_lev[player].notNil) {
						this.output(player, modeName, 0, gui.dbspec.map(gui.currentVal));
					};
				};
			} {
				// Fallback sur l'index
				if(p["output_mode"].notNil) {
					var idx2 = p["output_mode"].asInteger;
					defer {
						gui.currentModeIndex = idx2;
						gui.outputMenu.value_(idx2);
						gui.setupMode.(idx2);
						if(audio_group_lev[player].notNil) {
							this.output(player, gui.outputMenu.items[idx2].asString,
								0, gui.dbspec.map(gui.currentVal));
						};
					};
				};
			};
		};

		// --- Offsets ---
		if(p["offset_out_mono"].notNil) {
			gui.offset_out_mono = p["offset_out_mono"].asInteger;
			defer { gui.offset_out_mono_box !? { gui.offset_out_mono_box.value_(gui.offset_out_mono) } };
		};
		if(p["offset_out_stereo0"].notNil) {
			gui.offset_out_stereo0 = p["offset_out_stereo0"].asInteger;
			defer { gui.offset_out_stereo0_box !? { gui.offset_out_stereo0_box.value_(gui.offset_out_stereo0) } };
		};
		if(p["offset_out_stereo1"].notNil) {
			gui.offset_out_stereo1 = p["offset_out_stereo1"].asInteger;
			defer { gui.offset_out_stereo1_box !? { gui.offset_out_stereo1_box.value_(gui.offset_out_stereo1) } };
		};
		if(p["offset_out_multichannel"].notNil) {
			gui.offset_out_multichannel = p["offset_out_multichannel"].asInteger;
			defer { gui.offset_out_multichannel_box !? { gui.offset_out_multichannel_box.value_(gui.offset_out_multichannel) } };
		};
		if(p["offset_out_ambisonic"].notNil) {
			gui.offset_out_ambisonic = p["offset_out_ambisonic"].asInteger;
			defer { gui.offset_out_ambisonic_box !? { gui.offset_out_ambisonic_box.value_(gui.offset_out_ambisonic) } };
		};
		if(p["offset_out_aoo"].notNil) {
			gui.offset_out_aoo = p["offset_out_aoo"].asInteger;
			defer { gui.offset_out_aoo_box !? { gui.offset_out_aoo_box.value_(gui.offset_out_aoo) } };
		};

		// --- Amp ---
		if(p["amp"].notNil) {
			defer { gui.slider.valueAction_(p["amp"].asFloat) };
		};

		// --- Enable ---
		if(p["enabled"].notNil) {
			this.set_params(player, \enabled, p["enabled"].asInteger);
		};

		// --- Playing mode ---
		if(p["playing_mode"].notNil) {
			this.playing_mode(player, p["playing_mode"].asInteger);
		};

		// --- Weights ---
		if(p["weights"].notNil) {
			var w = p["weights"].asArray.collect(_.asFloat);
			defer { gui.weight_multislider.values_(w) };
			this.set_params(player, \weights, w);
		};

		// --- Continuity ---
		if(p["continuity"].notNil) {
			var v = p["continuity"].asFloat;
			defer { gui.continuitySlider.value_(v) };
			this.set_params(player, \continuity, v);
		};

		// --- Quality ---
		if(p["quality"].notNil) {
			var v = p["quality"].asFloat;
			defer { gui.qualitySlider.value_(v) };
			this.set_params(player, \quality, v);
		};

		// --- Probability ---
		if(p["probability"].notNil) {
			var v = p["probability"].asFloat;
			defer { gui.probSlider.value_(v) };
			this.set_params(player, \outputprobability, v);
		};

		// --- Time stretch slider ---
		if(p["time_stretch"].notNil) {
			var v = p["time_stretch"].asFloat;
			defer { gui.time_stretch_slider.value_(v) };
			this.set_time_stretch(player, v);
		};

		// --- Cut ---
		if(p["cut"].notNil) {
			var v = p["cut"].asInteger;
			defer { gui.cutButton.value_(v) };
			this.set_params(player, \cut, v);
		};

		// --- Sparse ---
		if(p["sparse"].notNil) {
			var v = p["sparse"].asInteger;
			defer { gui.sparseButton.value_(v) };
			this.set_params(player, \sparse, v);
		};

		// --- Timeout toggle + val ---
		if(p["timeout_toggle"].notNil) {
			var v = p["timeout_toggle"].asInteger;
			defer {
				gui.timeoutToggle.value_(v);
				gui.timeoutUpdateContent.(v);
			};
		};
		if(p["timeout_val"].notNil) {
			var v = p["timeout_val"].asFloat;
			gui.timeoutVal = v;
			this.set_time_out(player, v);  // ← set_time_out au lieu de set_params
		};
		// --- Time stretch toggle ---
		if(p["time_stretch_toggle"].notNil) {
			var v = p["time_stretch_toggle"].asInteger;
			defer { gui.timeStretchToggle.value_(v) };
			this.time_stretch_on_off(player, v);
		};

		// --- Beat align ---
		if(p["beat_align"].notNil) {
			var v = p["beat_align"].asInteger;
			defer { gui.beatAlignButton.value_(v) };
			this.set_params(player, \beat_align, v);
		};

		// --- Outgoing influence menu ---
		if(p["outgoing_influence"].notNil) {
			var v = p["outgoing_influence"].asInteger;
			defer { gui.outgoingInfluenceMenu.value_(v) };
			if(audio_influencer_info[player].notNil) {
				audio_influencer_info[player][5] = gui.outgoingInfluenceMenu.items[v];
			};
		};

		// --- Onset threshold ---
		if(p["onset_threshold"].notNil) {
			var v = p["onset_threshold"].asFloat;
			defer { gui.onsetThresholdSlider.value_(v) };
			if(audio_descriptors_onset[player].notNil) {
				server.sendMsg(\n_set, audio_descriptors_onset[player], *[\threshold, v]);
			};
		};

		// --- Onset limiter ---
		if(p["onset_limiter"].notNil) {
			var v = p["onset_limiter"].asFloat;
			defer { gui.onsetLimiterSlider.value_(v) };
			if(audio_influencer_info[player].notNil) {
				audio_influencer_info[player][0] = v;
			};
		};

		// --- Pitch quality ---
		if(p["pitch_quality"].notNil) {
			var v = p["pitch_quality"].asFloat;
			defer { gui.pitchQualitySlider.value_(v) };
		};

		// --- Transpositions ---
		if(p["transpositions"].notNil && gui.transpButtons.notNil) {
			var states = p["transpositions"];
			var semitoneValues = [-5, -4, -3, -2, -1, 1, 2, 3, 4, 5, 6];
			defer {
				// D'abord tout remettre à zéro OSC
				semitoneValues.do { |semi|
					this.remove_transform(player, semi);
				};
				// Puis appliquer les états sauvegardés
				states.do { |state, i|
					var v = state.asString.asInteger;
					gui.transpStates[i] = v;
					// Forcer l'état visuel via instVarPut
					gui.transpButtons[i].instVarPut(
						gui.transpButtons[i].class.instVarNames.indexOf(\isPressed),
						v == 1
					);
					gui.transpButtons[i].refresh;
					// Envoyer OSC seulement si actif
					if(v == 1) {
						this.add_transform(player, semitoneValues[i]);
					};
				};
			};
		};

		// --- Corpus ---
		if(p["corpus"].notNil && (p["corpus"].asString.size > 0)) {
			var corpusName = p["corpus"].asString;
			// Trouver l'index dans le menu par le nom
			var idx = gui.corpus_menu.items.detectIndex({ |item|
				item.asString.drop(4) == corpusName
			});
			if(idx.notNil) {
				defer { gui.corpus_menu.value_(idx) };
				this.load_corpus(player, corpusName, nil, nil, nil, false);
			} {
				("Preset: corpus not found in menu -> " ++ corpusName).postln;
			};
		};

		defer {
			if(player_gui_dico[player].notNil) {
				player_gui_dico[player].current_preset = preset_name;
			};
		};
		("Preset loaded: " ++ preset_name).postln;
		preset_model.setParam(\preset_loaded, preset_name);
	}

	// ---- SESSION : sauvegarder corpus_path + devices ----
	save_session {
		var data = Dictionary.new;

		data["corpus_path"] = corpus_path ?? "";
		data["in_device"]   = server.options.inDevice ?? "";
		data["out_device"]  = server.options.outDevice ?? "";

		File.use(session_path, "w", { |f|
			f.write(data.asJSON);
		});
	}

	// ---- SESSION : charger corpus_path + devices ----
	load_session {
		var json_str, data;

		if(File.exists(session_path).not) { ^nil };

		json_str = File.use(session_path, "r", { |f| f.readAllString });
		data = json_str.parseJSON;

		if(data["corpus_path"].notNil && (data["corpus_path"].asString.size > 0)) {
			corpus_path = data["corpus_path"].asString;
			model_server.setParam(\corpus_path, corpus_path);
		};
		if(data["in_device"].notNil && (data["in_device"].asString.size > 0)) {
			server.options.inDevice = data["in_device"].asString;
		};
		if(data["out_device"].notNil && (data["out_device"].asString.size > 0)) {
			server.options.outDevice = data["out_device"].asString;
		};
		"Session loaded.".postln;
	}

	// ---- PRESET : lister les presets disponibles ----
	list_presets {
		var files = (preset_path ++ "*.json").pathMatch;
		^files.collect({ |f|
			f.basename.splitext[0]  // nom sans extension
		}).sort;
	}

	// ---- Helper : sérialiser un Dictionary/Event en JSON ----
	dict_to_json { |obj|
		^obj.asJSON   // SuperCollider >= 3.12 a asJSON natif
		// Si asJSON n'est pas disponible, utilise la méthode manuelle ci-dessous
	}

	// Hacer level y RT record
	level {|player, lev l|
		if(players[player.asSymbol][0] == \audio)
		{
			// "toto".postln;
			// lev.postln;
			audio_group_lev[player.asSymbol].set(\amp, lev);
			player_gui_dico[player.asSymbol].lev(lev);
			// audio_group_lev.add(player.asSymbol -> Synth(\audioOut8, target: audio_group[player.asSymbol], addAction:'addToTail'));

		}
	}

	speakers {|player, speakers l|
		if(players[player.asSymbol][0] == \audio)
		{
			players_info[player.asSymbol].postln;
			players_info[player.asSymbol][1].postln;
			players_info[player.asSymbol][1] = speakers;
			players_info[player.asSymbol][1].postln;
		}
	}


	load_midi_instrument {|player, midi_instr|
		vst_instrument.add(player.asSymbol -> Synth(\vstinstr));
		midi_instrument.add(player.asSymbol -> VSTPluginController(vst_instrument[player.asSymbol]).open(midi_instr));
	}

	midi_instrument_editor {|player|
		if(midi_instrument[player.asSymbol].notNil)
		{
			midi_instrument[player.asSymbol].editor;
		}
		{
			"VST instrument no yet loaded".postln;
		}
	}

	recombine {|player, on_off|
		case
		{on_off == 1} { players_recv_osc[player.asSymbol].sendMsg(player.asSymbol,  \set_recombine, \True)	}
		{on_off == 0} { players_recv_osc[player.asSymbol].sendMsg(player.asSymbol,  \set_recombine, \False)	}
	}

	delete_agent {|player|
		if(players[player.asSymbol].notNil)
		{
			case
			{players[player.asSymbol][0] == \midi}
			{
				// All midi notes off
				midi_instrument[player.asSymbol].midi.allNotesOff(1);
				vst_instrument[player.asSymbol].free;
				midipitchclass[player.asSymbol].routine.stop; // stop pitchclass computation
				midipitchclass.removeAt(player.asSymbol);
			}
			{players[player.asSymbol][0] == \audio}
			{
				/*"DELETE1".postln;
				nodes[player.asSymbol][0].postln;
				"DELETE2".postln;
				nodes[player.asSymbol].postln;
				server.sendMsg(\n_set, nodes[player.asSymbol][0], \free, 0);*/
				nodes.removeAt(player.asSymbol);
				audio_influencer_bus[player.asSymbol].free;
				audio_influencer_bus.removeAt(player.asSymbol);
				audio_group[player.asSymbol].free;
				audio_group.removeAt(player.asSymbol);
				blinking.removeAt(player.asSymbol);
			};
			players[player.asSymbol].drop(1).collect({|ply| // remove player from midi_influencer_dico
				"ply_insideSomax".postln;
				ply.postln;
				if(midi_influencer_dico[ply.asSymbol].notNil)
				{
					if(midi_influencer_dico[ply.asSymbol].includes(player))
					{
						// midi_influencer_dico[ply.asSymbol].removeAt(midi_influencer_dico[ply].indexOf(player));
						midi_influencer_dico[ply.asSymbol].remove(player);
					}
				};
				if(audio_influencer_dico[ply.asSymbol].notNil)
				{
					if(audio_influencer_dico[ply.asSymbol].includes(player))
					{
						// audio_influencer_dico[ply.asSymbol].removeAt(audio_influencer_dico[ply].indexOf(player));
						audio_influencer_dico[ply.asSymbol].remove(player);
					}
				}
			});
			if(midi_influencer_dico[player.asSymbol].notNil)
			{
				midi_influencer_dico[player.asSymbol].collect({|ply|
					if(players.includesKey(ply))
					{
						// players[ply].removeAt(midi_influencer_dico[ply].indexOf(player));
						players[ply].remove(player);
					}
				});
				midi_influencer_dico.removeAt(player);
			};
			if(audio_influencer_dico[player.asSymbol].notNil)
			{
				audio_influencer_dico[player.asSymbol].collect({|ply|
					if(players.includesKey(ply))
					{
						// players[ply].removeAt(audio_influencer_dico[ply].indexOf(player));
						players[ply].remove(player);
					}
				});
				// remove descriptors
				audio_descriptors_mfcc.removeAt(player.asSymbol);
				audio_descriptors_chroma.removeAt(player.asSymbol);
				audio_descriptors_pitch.removeAt(player.asSymbol);
				audio_descriptors_onset.removeAt(player.asSymbol);
				audio_influencer_dico.removeAt(player);
			};

			players.removeAt(player.asSymbol);
			// "remove_index".postln;
			// agent.detect({|x| x.key == player.asSymbol}).value.postln;
			agent.do({|item, i|
				if(item.key == player.asSymbol)
				{
					// i.postln;
					agent.removeAt(i);
				}
			});
			model_server.setParam(\delete_agent, agent.collect({|x| x.key}).reverse);

			// agent.removeAt(agent.detect({|x| x.key == player.asSymbol}).value); // retrieve index value from List
			osc_to_server.sendMsg('/somax', \delete_agent, player.asSymbol); // remove from python server
			OSCdef(player.asSymbol).free; // unregister OSC
		}
		{
			"This agent does not exist".postln;
		}
	}

	load_synthdefs {
		"load_synthdefs".postln;

		server.bind{

			//// Chroma
			SynthDef(\Fluid_Chroma_So, {|in = 0, id = 10, numChroma = 24, ref = 440, normalize = 0, minFreq = 0, maxFreq = -1, windowSize = 8192, hopSize = 512, fftSize = -1, maxFFTSize = -1, maxNumChroma, trig_rate = 30, lagTimeU = 0.001, lagTimeD = 0.1|
				var sig, trig, chroma;

				sig = In.ar(in);
				trig = Impulse.kr(trig_rate);
				/*			sig = FluidChroma.kr(sig, numChroma:numChroma, ref:ref, normalize:normalize, minFreq:minFreq, maxFreq:maxFreq, windowSize:windowSize, hopSize:hopSize, fftSize:fftSize, maxFFTSize:maxFFTSize);*/
				chroma = FluidChroma.kr(sig, windowSize: windowSize, hopSize: hopSize, normalize:1);
				// chroma = FluidStats.kr(sig, 40)[0]; // index 0 to get just the means
				/*			sig = LagUD.kr(sig, lagTimeU, lagTimeD);*/
				// sig.poll;
				SendReply.kr(trig, '/chroma', chroma, id);
			}).load;

			server.sync;

			//// MFCC
			SynthDef(\Fluid_MFCC_So, {|in = 0, id = 10, numCoeffs = 14, windowSize = 4096, hopSize = 1024, trig_rate = 30, lagTimeU = 0.001, lagTimeD = 0.1|
				var sig, trig, mfcc, normFactor;

				sig = In.ar(in);
				trig = Impulse.kr(trig_rate);
				mfcc = FluidMFCC.kr(sig, numCoeffs:14, startCoeff:0, maxNumCoeffs:14, windowSize:windowSize, hopSize:hopSize);

				// Normalisation à la Librosa de Somax (pour coller à ircamdescriptors~) : mfcc * sqrt(2 / numCoeffs)
				normFactor = (2 / numCoeffs).sqrt;
				mfcc = mfcc * normFactor;

				SendReply.kr(trig, '/mfcc', mfcc, id);
			}).load;

			server.sync;

			SynthDef(\Fluid_Pitch_So, {|in = 0, id = 10, algorithm = 2, minFreq = 20, maxFreq = 10000, unit = 1, windowSize = 4096, hopSize = 2048, fftSize = -1, trig_rate = 30, lagTimeU = 0.001, lagTimeD = 0.1|
				var sig, trig, freq, conf;

				sig = In.ar(in);
				trig = Impulse.kr(trig_rate);
				# freq, conf = FluidPitch.kr(sig, algorithm: 2, minFreq: minFreq, maxFreq: maxFreq, unit: unit, windowSize: windowSize, hopSize: hopSize, fftSize: fftSize); // Yin
				// sig = LagUD.kr(sig, lagTimeU, lagTimeD);

				SendReply.kr(trig, '/fluid_pitch', [freq,conf], id);
			}).load;

			server.sync;

			SynthDef(\Fluid_Onset_So, {|in = 0, xFade = 1, trig = 0, amp = 0,  id = 10, matrix_ramp = 0.01, gate = 1, free = 1, metric = 9, threshold = 0.15, minSliceLength = 45, filterSize = 9, frameDelta = 0, windowSize = 128, hopSize = 64, fftSize = -1|
				var sig;

				sig = In.ar(in);
				sig = FluidOnsetSlice.ar(sig, metric, threshold, minSliceLength, filterSize, frameDelta, windowSize, hopSize, fftSize);
				// onsets.poll;
				SendReply.ar(sig, '/onsetdetect', 1, id);
			}).load;

			server.sync;

			SynthDef(\Play_stereo, { |out = 0, outbus = -1, buf, rev = 1, envbuf = -1, midicents = 0, pos = 0, loop = 0, amp = 0, t_trig , matrix_in = 0, matrix_out = 0.01, updateFreq = 10, peakLag = 0, index = -1,  gate = 1, free = 1|

				var sig, trigRate2, envgate, envpause, sig_mix;
				var rate = 2.pow(midicents / 1200); // conversion midicents -> ratio

				envgate = EnvGen.kr(Env.asr(matrix_in, 1.0, matrix_out, \welch ), free, doneAction:2);
				envpause = EnvGen.kr(Env.asr(matrix_in, 1.0, matrix_out, \welch ), gate, doneAction:1);

				sig = PlayBuf.ar(2, buf, BufRateScale.kr(buf) * rate * rev, t_trig, BufFrames.kr(buf) * pos, loop: loop); //, doneAction:2); //este libera synth para performance

				sig = sig * envgate * envpause * amp.dbamp.lag;
				sig_mix = Mix.ar(sig);
				SendPeakRMS.kr(sig, updateFreq, peakLag, '/meter', index);
				// Out.ar(out, sig);
				Out.ar(outbus, sig_mix)
			}).load;

			server.sync;

			SynthDef(\AudioIn, {|input = 0, outbus = 0, matrix_ramp = 0.01, amp = 0, inlev = 0, updateFreq = 10, peakLag = 0, index = -1, gate = 1, free = 1|
				var sig, envgate, envpause;

				envgate = EnvGen.ar(Env.asr(matrix_ramp, 1.0, matrix_ramp, \welch ), free, doneAction:2);
				sig = SoundIn.ar(input);
				sig = sig * envgate * amp.dbamp.lag(1);
				SendPeakRMS.kr(sig, updateFreq, peakLag, '/meter', index);
				Out.ar(outbus, sig*amp.dbamp.lag)
			}).load;

			server.sync;

			// Play slices

			SynthDef(\Play_slice_2, { |outbus = -1, buf, startsamp, stopsamp, midicents = 0,
				amp = 0, fade = 0.03, free = 1, loop = 0, t_trig = 0|

				var buf_frames = BufFrames.kr(buf);
				var sr = BufSampleRate.ir(buf);
				var start = (startsamp * 0.001) * sr;
				var end = (stopsamp * 0.001) * sr;
				var rate = 2.pow(midicents / 1200); // conversion midicents -> ratio
				var sig = PlayBuf.ar(2, buf, BufRateScale.kr(buf) * rate, t_trig, start, loop: loop, doneAction: 2);

				var envgate = EnvGen.kr(Env.asr(fade, 1.0, fade, \welch), free, doneAction: 2);
				sig = sig * envgate;

				Out.ar(outbus, sig * amp.dbamp);
			}).load;


			server.sync;

			SynthDef(\Play_slice_24, { |out = 0, outbus = -1, buf, startsamp, stopsamp, midicents = 0,
				amp = 0, fade = 0.03, free = 1, loop = 0, t_trig = 0|

				var buf_frames = BufFrames.kr(buf);
				var sr = BufSampleRate.ir(buf);
				var start = (startsamp*0.001)*sr;
				var end = (stopsamp*0.001)*sr;
				var rate = 2.pow(midicents / 1200); // conversion midicents -> ratio
				var sig = PlayBuf.ar(24, buf, BufRateScale.kr(buf) * rate, t_trig, start, loop: loop, doneAction:2);
				var dursecs = BufDur.kr(buf); //(end - start ) ;
				var envgate, sig_mix;
				envgate = EnvGen.kr(Env.asr(fade, 1.0, fade, \welch ), free, doneAction:2);
				sig = sig * envgate;
				// sig_mix = Mix.ar(sig);
				// Out.ar(out, sig * amp.dbamp);
				Out.ar(outbus, sig*amp.dbamp)
			}).load;

			server.sync;

			SynthDef(\Play_slice_ambi, { |out = 0, buf, startsamp, stopsamp, midicents = 0,
				amp = 0, fade = 0.03, free = 1, loop = 0, t_trig = 0, seg = 0, index, cluster = 0, y = 0, x = 0, z = 0|

				var buf_frames = BufFrames.kr(buf);
				var sr = BufSampleRate.ir(buf);
				var start = (startsamp*0.001)*sr;
				var end = (stopsamp*0.001)*sr;
				// var start = Index.kr(idx, index); // look up the start position
				// var dur_samps = Index.kr(idx, index + 1) - start; // calculate the duration in samples
				// var dur_sec = min(dur_samps / BufSampleRate.ir(buf),1);
				// var end = (stopsamp*0.001)*sr;
				var rate = 2.pow(midicents / 1200); // conversion midicents -> ratio
				var sig = PlayBuf.ar(2, buf, BufRateScale.kr(buf) * rate, t_trig, start, loop: loop, doneAction:2);
				var dursecs = BufDur.kr(buf); //(end - start ) ;
				var envgate, sig_mix, done;
				var choose_cluster, sel_hp, speakers, cluster_amp, sel_amp;

				envgate = EnvGen.kr(Env.asr(fade, 1.0, fade, \welch ), free, doneAction:2);

				speakers = \speakers.kr([[0, 7], [8, 13], [14, 17]]);
				// speakers = \speakers.kr([[0, 1], [6, 9], [10, 13]]);
				cluster_amp = \cluster_amp.kr([0, 0, 0]);

				sel_hp = Select.kr(cluster, speakers);
				sel_amp = Select.kr(cluster, cluster_amp);
				// sel_hp.poll;
				choose_cluster = TIRand.kr(sel_hp[0], sel_hp[1], trig: t_trig);
				sig = sig * envgate *sel_amp.dbamp;
				done = Done.kr(envgate);
				SendTrig.kr(done, 0, seg);
				sig_mix = Mix.ar(sig);
				sig = AmbisonicEncoderV2Cart7In1.ar(sig_mix, 0, y, x.neg, z);
				Out.ar(out, sig * amp.dbamp);
				// ReplaceOut.ar(outbus, sig_mix*amp.dbamp)
			}).load;

			server.sync;

			/// For using with AOO system
			SynthDef(\Play_slice_aoo, { |out = 0, outbus = -1, buf, startsamp, stopsamp, midicents = 0,
				amp = 0, fade_in = 0.03, fade_out = 0.03, free = 1, loop = 0, t_trig = 0, seg = 0, index, cluster = 0|

				var buf_frames = BufFrames.kr(buf);
				var sr = BufSampleRate.ir(buf);
				var start = (startsamp*0.001)*sr;
				var end = (stopsamp*0.001)*sr;
				// var start = Index.kr(idx, index); // look up the start position
				// var dur_samps = Index.kr(idx, index + 1) - start; // calculate the duration in samples
				// var dur_sec = min(dur_samps / BufSampleRate.ir(buf),1);
				// var end = (stopsamp*0.001)*sr;
				var rate = 2.pow(midicents / 1200); // conversion midicents -> ratio
				var sig = PlayBuf.ar(2, buf, BufRateScale.kr(buf) * rate, t_trig, start, loop: loop, doneAction:0);
				var dursecs = BufDur.kr(buf); //(end - start ) ;
				var envgate, sig_mix, done;
				var choose_cluster, sel_hp, speakers, cluster_amp, sel_amp;

				envgate = EnvGen.kr(Env.asr(fade_in, 1.0, fade_out, \welch ), free, doneAction:2);

				// speakers = \speakers.kr([[0, 2], [3, 5], [6, 7], [8, 9]]); // 16 speakers
				speakers = \speakers.kr([[0, 2], [3, 4], [5, 6], [7, 8]]); // 9 speakers
				// speakers = \speakers.kr([[0, 3], [4, 7], [8, 11], [12, 15]]); // 16 speakers
				// speakers = \speakers.kr([[0, 3], [4, 8], [9, 13], [14, 17]]); // 18 speakers
				// speakers = \speakers.kr([[0, 5], [6, 11], [12, 17], [18, 23]]); // 24 speakers
				// speakers = \speakers.kr([[0, 1], [6, 9], [10, 13]]);
				cluster_amp = \cluster_amp.kr([0, 0, 0]);

				sel_hp = Select.kr(cluster, speakers);
				sel_amp = Select.kr(cluster, cluster_amp);
				// sel_hp.poll;
				choose_cluster = TIRand.kr(sel_hp[0], sel_hp[1], trig: t_trig);
				sig = sig * envgate *sel_amp.dbamp;
				done = Done.kr(envgate);
				SendTrig.kr(done, 0, seg);
				sig_mix = Mix.ar(sig);
				Out.ar(outbus+choose_cluster, sig * amp.dbamp);
				// ReplaceOut.ar(outbus, sig_mix*amp.dbamp)
			}).load;

			server.sync;

			// Mono cluster
			SynthDef(\Play_slice_aoo2, { |out = 0, outbus = 5, ch_offset = 5, buf, startsamp, stopsamp, midicents = 0,
				amp = 0, fade = 0.03, free = 1, loop = 0, t_trig = 0, seg = 0, index, cluster = 0|

				var buf_frames = BufFrames.kr(buf);
				var sr = BufSampleRate.ir(buf);
				var start = (startsamp*0.001)*sr;
				var end = (stopsamp*0.001)*sr;
				// var start = Index.kr(idx, index); // look up the start position
				// var dur_samps = Index.kr(idx, index + 1) - start; // calculate the duration in samples
				// var dur_sec = min(dur_samps / BufSampleRate.ir(buf),1);
				// var end = (stopsamp*0.001)*sr;
				var rate = 2.pow(midicents / 1200); // conversion midicents -> ratio
				var sig = PlayBuf.ar(2, buf, BufRateScale.kr(buf) * rate, t_trig, start, loop: loop, doneAction:2);
				var dursecs = BufDur.kr(buf); //(end - start ) ;
				var envgate, sig_mix, done;
				var choose_cluster, sel_hp, speakers, cluster_amp, sel_amp;
				var num_clusters = 10;

				envgate = EnvGen.kr(Env.asr(fade, 1.0, fade, \welch ), free, doneAction:2);

				// speakers = \speakers.kr([[0, 7], 0]);
				// speakers = \speakers.kr([0, 7]++Array.fill2D(num_clusters-1, 2, 0));
				// speakers = \speakers.kr([[0, 1], [2, 3], [6, 9], [10, 13]]);
				// speakers = \speakers.kr([[0, 7], [8, 13], [14, 17]]);
				// speakers = \speakers.kr([[17, 24], [2, 3], [4, 5], [6, 7], [8, 9]]); // max 5 clusters
				speakers = \speakers.kr([[0, 2], [3, 4], [5, 6], [7, 8]]); // 9 speakers
				cluster_amp = \cluster_amp.kr([0]);
				// speakers.poll;

				sel_hp = Select.kr(cluster, speakers);
				// sel_hp = [6, 13];
				sel_amp = Select.kr(cluster, cluster_amp);
				// sel_amp = 0;
				// sel_hp.poll;
				choose_cluster = TIRand.kr(sel_hp[0], sel_hp[1], trig: t_trig);
				// choose_cluster = speakers[0];
				sig = sig * envgate *sel_amp.dbamp;
				done = Done.kr(envgate);
				SendTrig.kr(done, 0, seg);
				sig_mix = Mix.ar(sig);
				// choose_cluster.poll;
				// outbus.poll;
				Out.ar(choose_cluster+outbus+ch_offset, sig * amp.dbamp);
				// ReplaceOut.ar(outbus, sig_mix*amp.dbamp)
			}).load;

			server.sync;

			/// VST instrument
			SynthDef.new(\vstinstr, { arg out = 0, bypass = 0;
				// VST instruments usually don't have inputs
				Out.ar(out, VSTPlugin.ar(nil, 2, bypass));
			}).load;
			server.sync;
			SynthDef.new(\vst1, { | bus = 0, bypass |
				ReplaceOut.ar(bus, VSTPlugin.ar(In.ar(bus, 1), 1, bypass));
			}).add;
			server.sync;
			SynthDef.new(\vst2, { | bus = 0, bypass |
				ReplaceOut.ar(bus, VSTPlugin.ar(In.ar(bus, 2), 2, bypass));
			}).add;
			server.sync;
			SynthDef.new(\vst16, { | bus = 0, bypass |
				ReplaceOut.ar(bus, VSTPlugin.ar(In.ar(bus, 16), 16, bypass));
			}).add;
			server.sync;
			SynthDef.new(\vst18, { | bus = 0, bypass |
				ReplaceOut.ar(bus, VSTPlugin.ar(In.ar(bus, 18), 18, bypass));
			}).add;
			server.sync;
			SynthDef.new(\vst24, { | bus = 0, bypass |
				ReplaceOut.ar(bus, VSTPlugin.ar(In.ar(bus, 24), 24, bypass));
			}).add;

			server.sync;
			this.descriptors_osc_replay;
		}
	}

	descriptors_osc_replay {
		/// SendReplay OSC descriptors
		var lastPitch = nil, minDelay = 0.1, pitchTolerance = 1, minQuality = 0.75, shouldAcceptPitch;

		// Fonction d’évaluation du message pour une instance donnée
		/*		shouldAcceptPitch = {
		arg pitch, key, time = Main.elapsedTime;
		var lastTime = lastPitchTimes[key] ?? 0;
		var lastPitch = lastAcceptedPitches[key];

		var isFarEnough = (time - lastTime) > minDelay;
		var isOutsideTolerance = {
		lastPitch.isNil or: {
		(pitch - lastPitch).abs > pitchTolerance
		}
		};

		if (isFarEnough and: isOutsideTolerance.value) {
		lastPitchTimes[key] = time;
		lastAcceptedPitches[key] = pitch;
		true
		} {
		false
		}
		};*/
		// Fonction de filtrage
		shouldAcceptPitch = {
			arg pitch, quality, key, time;
			var lastTime, lastPitch, isFarEnough, isOutsideTolerance;
			time = time ?? Main.elapsedTime;

			lastTime = lastPitchTimes[key] ?? 0;
			lastPitch = lastAcceptedPitches[key];

			isFarEnough = (time - lastTime) > minDelay;
			isOutsideTolerance = {
				lastPitch.isNil or: {
					(pitch - lastPitch).abs > pitchTolerance
				}
			};

			if ((quality >= minQuality) and: isFarEnough and: isOutsideTolerance.value) {
				lastPitchTimes[key] = time;
				lastAcceptedPitches[key] = pitch;
				// ("Pitch accepté : " ++ pitch.round(1) ++ " (qualité : " ++ quality.round(2) ++ ")").postln;
				true
			} {
				// ("Pitch ignoré : " ++ pitch.round(1) ++ " (qualité: " ++ quality.round(2) ++ ")").postln;
				false
			}
		};

		OSCdef(\mfcc, {|msg|
			var mfcc_array, influencer, win_visible;
			// msg.postln;
			influencer = audio_descriptors_mfcc.findKeyForValue(msg[2]); // retrieve influencer

			// msg[3..].postln;

			if(audio_descriptors_mfcc.includes(msg[2]) && audio_influencer_info[influencer.asSymbol][3]) // if sendreply id exist
			{
				audio_descriptors[audio_descriptors_mfcc.findKeyForValue(msg[2])][2] = msg;

/*				audio_influencer_dico[influencer.asSymbol].collect({|dest| // send onset to all connected Players

					players_recv_osc[dest.asSymbol].sendMsg(dest, \influence, \mfcc, \mfcc, *msg[3..]);

				});*/
			};

			if(influencer_gui_dico.includesKey(influencer.asSymbol)) // if influencer GUI active
			{
				influencer_gui_dico[influencer.asSymbol].mfcc(msg[3..]);
			};

			if(player_gui_dico.includesKey(influencer.asSymbol)) // if player GUI active
			{
				defer {
					win_visible = player_gui_dico[influencer].win.view.visible;
					if(win_visible) // if player GUI active
					{
						player_gui_dico[influencer.asSymbol].mfcc(msg[3..]);
					}
				}
			}
		}, '/mfcc');



		OSCdef(\chroma, {|msg|
			var chroma_array, influencer, win_visible;
			influencer = audio_descriptors_chroma.findKeyForValue(msg[2]); // retrieve influencer
			// msg.postln;
			// audio_influencer_info[influencer.asSymbol][3].postln;
			/*			"chroma_influencer".postln;
			influencer.postln;*/

			// msg[3..].postln;

			if(audio_descriptors_chroma.includes(msg[2]) && audio_influencer_info[influencer.asSymbol][3]) // if sendreply id exist
			{
				audio_descriptors[audio_descriptors_chroma.findKeyForValue(msg[2])][1] = msg;

/*				audio_influencer_dico[influencer.asSymbol].collect({|dest| // send onset to all connected Players

					players_recv_osc[dest.asSymbol].sendMsg(dest, \influence, \harmonic, \chroma, *msg[3..]);


				});*/
			};

			if(influencer_gui_dico.includesKey(influencer.asSymbol)) // if influencer GUI active
			{
				influencer_gui_dico[influencer.asSymbol].chroma(msg[3..]);
			};

			// NEW
			if(player_gui_dico.includesKey(influencer.asSymbol)) // if player GUI active
			{
				defer {
					win_visible = player_gui_dico[influencer].win.view.visible;
					if(win_visible) // if player GUI active
					{
						player_gui_dico[influencer.asSymbol].chroma(msg[3..]);
					}
				}
			}


			/////
			/*			if(msg[2] == audio_descriptors_synth[influencer++"chroma"]) // if sendreplay id == node_id
			{
			audio_descriptors[influencer.asSymbol][1] = msg[3..];
			}*/
			// ~chroma = msg[3..];
		}, '/chroma');

		OSCdef(\pitch, {|msg|
			var pitch_a, quality, influencer, chroma_array, mfcc_array, win_visible;


			influencer = audio_descriptors_pitch.findKeyForValue(msg[2]); // retrieve influencer
			minDelay = audio_influencer_info[influencer.asSymbol][0]*0.001;
			minQuality =  audio_influencer_info[influencer.asSymbol][4];

			// influencer.postln;
			if(audio_descriptors_pitch.includes(msg[2])  && audio_influencer_info[influencer.asSymbol][3]) // if sendreplay id exist
			{
				pitch_a = msg[3..][0]; // .cpsmidi.round.asInteger;
				quality = msg[3..][1];

				if (shouldAcceptPitch.(pitch_a, quality, influencer)) {
					// ("Pitch accepté : " ++ pitch_a.round(1)).postln;

					audio_descriptors[audio_descriptors_pitch.findKeyForValue(msg[2])][0] = msg[3..];

/*					audio_influencer_dico[influencer.asSymbol].collect({|dest| // send onset to all connected Players

						players_recv_osc[dest.asSymbol].sendMsg(dest, \influence, \melodic, \pitch, pitch_a);

					});*/

					if(influencer_gui_dico.includesKey(influencer.asSymbol)) // if influencer GUI active
					{
						// msg[3..][0].postln;
						influencer_gui_dico[influencer.asSymbol].pitch(pitch_a.round.asInteger);
					};
					if(audio_influencer_info[influencer.asSymbol][5] == "PitchOnset") // if sendreply id exist
					{

						// Retreive descriptors from influencer
						mfcc_array = audio_descriptors[influencer.asSymbol][2];
						chroma_array = audio_descriptors[influencer.asSymbol][1];

						audio_influencer_dico[influencer.asSymbol].collect({|dest| // send onset to all connected Players
							// dest.postln;

							// Send influences to Python server
							players_recv_osc[dest.asSymbol].sendMsg(dest, \influence, \melodic, \pitch, pitch_a.round.asInteger);
							players_recv_osc[dest.asSymbol].sendMsg(dest, \influence, \mfcc, \mfcc, *mfcc_array[3..]);
							players_recv_osc[dest.asSymbol].sendMsg(dest, \influence, \harmonic, \chroma, *chroma_array[3..]);
							players_recv_osc[dest.asSymbol].sendMsg(dest, \bang);
							// chroma_array.postln;
							// dest.postln;

							defer {
								win_visible = player_gui_dico[dest].win.view.visible;
								if(win_visible) // if player GUI active
								{
									player_gui_dico[dest.asSymbol].analyse(pitch_a, chroma_array[3..], mfcc_array[3..]);
								}
							};
						});
						// "influencer_somax_arrives1".postln;
						// influencer_gui_dico.postln;
						if(influencer_gui_dico.includesKey(influencer.asSymbol)) // if influencer GUI active
						{
							influencer_gui_dico[influencer.asSymbol].blink;
						};
						if(player_gui_dico.includesKey(influencer.asSymbol)) // if player GUI active
						{
							// player_gui_dico[influencer.asSymbol].blink(pitch_a, chroma_array, mfcc_array);
							player_gui_dico[influencer.asSymbol].blink;

						}
					};
					if(player_gui_dico.includesKey(influencer.asSymbol)) // if player GUI active
					{
						defer {
							win_visible = player_gui_dico[influencer].win.view.visible;
							if(win_visible) // if player GUI active
							{
								player_gui_dico[influencer.asSymbol].pitch(pitch_a.round.asInteger);
							}
						}
					}
				};

				// lastPitchTimes.postln;

				/*					lastPitch = pitch_a;
				lastPitchTimes[influencer].add(pitch_a -> now);*/

				/*					// Traite la note ici
				} {
				// ("Note ignorée: " ++ pitch_a).postln;
				};*/


			}

			/*			if(msg[2] == audio_descriptors_synth[influencer++"pitch"])
			{
			audio_descriptors[influencer.asSymbol][0] = msg[3..];
			}*/
		}, '/fluid_pitch');

		OSCdef(\onset, {|msg|
			var chroma_array, mfcc_array, pitch_a, influencer, win_visible;

			influencer = audio_descriptors_onset.findKeyForValue(msg[2]); // retrieve influencer

			// audio_influencer_info[influencer.asSymbol][3].postln;
			if((audio_descriptors_onset.includes(msg[2])) && (audio_influencer_info[influencer.asSymbol][3]) && (audio_influencer_info[influencer.asSymbol][5]) == "Onset" ) // if sendreply id exist
			{

				// Retreive descriptors from influencer
				pitch_a = (audio_descriptors[influencer.asSymbol][0][0]).cpsmidi.round.asInteger; // convert pitch to midi
				mfcc_array = audio_descriptors[influencer.asSymbol][2];
				chroma_array = audio_descriptors[influencer.asSymbol][1];

				audio_influencer_dico[influencer.asSymbol].collect({|dest| // send onset to all connected Players
					// Send influences to Python server
					// Send influences to Python server
					players_recv_osc[dest.asSymbol].sendMsg(dest, \influence, \melodic, \pitch, pitch_a.round.asInteger);
					players_recv_osc[dest.asSymbol].sendMsg(dest, \influence, \mfcc, \mfcc, *mfcc_array[3..]);
					players_recv_osc[dest.asSymbol].sendMsg(dest, \influence, \harmonic, \chroma, *chroma_array[3..]);
					players_recv_osc[dest.asSymbol].sendMsg(dest, \bang);

					defer {
						win_visible = player_gui_dico[dest].win.view.visible;
						if(win_visible) // if player GUI active
						{
							player_gui_dico[dest.asSymbol].analyse(pitch_a.round.asInteger, chroma_array[3..], mfcc_array[3..]);
						}
					};
				});
				// "influencer_somax_arrives1".postln;
				// influencer_gui_dico.postln;
				if(influencer_gui_dico.includesKey(influencer.asSymbol)) // if influencer GUI active
				{
					influencer_gui_dico[influencer.asSymbol].blink;
				};
				if(player_gui_dico.includesKey(influencer.asSymbol)) // if player GUI active
				{
					player_gui_dico[influencer.asSymbol].blink; //(pitch_a.round.asInteger, chroma_array, mfcc_array);
				};
				// audio_influencer_info[influencer.asSymbol].postln;
				audio_influencer_info[influencer.asSymbol][3] = false;
				{ // antirebond
					(audio_influencer_info[influencer.asSymbol][0]*0.001).wait;
					audio_influencer_info[influencer.asSymbol][3] = true;
				}.fork;
			}

		}, '/onsetdetect');
	}


	play_slice {|player, startsamp, stopsamp = nil, pitchshift|
		// var newNodeID = server.nextNodeID;

		nodes[player.asSymbol][1] = server.nextNodeID+10;
		/*		"nodes0 ".postln;
		(nodes[player.asSymbol]).postln;
		"nodes0 ".postln;*/
		// ("previousNodeID1 "+previousNodeID).postln;
		// Arrêter l'ancien nœud si nécessaire
		if(nodes[player.asSymbol][0].notNil)
		{
			server.sendMsg(\n_set, nodes[player.asSymbol][0], \free, 0);
			// ("stop "+previousNodeID).postln;
		};
		if(startsamp == "off" && nodes[player.asSymbol][0].notNil)
		{
			server.sendMsg(\n_set, nodes[player.asSymbol][0], \free, 0);
			"off".postln;
			// this.release;
		} {
			// Jouer une nouvelle synthèse

			server.sendMsg(\s_new, \Play_slice_2, nodes[player.asSymbol][1], 0, audio_group[player].nodeID, \startsamp, startsamp,  \buf, buffers[player.asSymbol].bufnum, \midicents, pitchshift, \outbus, audio_influencer_bus[player.asSymbol].index);
			nodes[player.asSymbol][0] = nodes[player.asSymbol][1];
		}
	}

	play_slice24 {|player, startsamp, stopsamp = nil, pitchshift|
		nodes[player.asSymbol][1] = server.nextNodeID+10;
		if(nodes[player.asSymbol][0].notNil)
		{
			server.sendMsg(\n_set, nodes[player.asSymbol][0], \free, 0);
			// ("stop "+previousNodeID).postln;
		};
		if(startsamp == "off" && nodes[player.asSymbol][0].notNil)
		{
			server.sendMsg(\n_set, nodes[player.asSymbol][0], \free, 0);
			"off".postln;
			// this.release;
		} {
			// Jouer une nouvelle synthèse
			server.sendMsg(\s_new, \Play_slice_24, nodes[player.asSymbol][1], 0, audio_group[player].nodeID, \startsamp, startsamp,  \buf, buffers[player.asSymbol].bufnum, \midicents, pitchshift, \outbus, audio_influencer_bus[player.asSymbol].index);
			nodes[player.asSymbol][0] = nodes[player.asSymbol][1];
		}
	}

	play_slice_ambi {|player, startsamp, stopsamp = nil, pitchshift, x, y, z|
		nodes[player.asSymbol][1] = server.nextNodeID+10;
		if(nodes[player.asSymbol][0].notNil)
		{
			server.sendMsg(\n_set, nodes[player.asSymbol][0], \free, 0);
			// ("stop "+previousNodeID).postln;
		};
		if(startsamp == "off" && nodes[player.asSymbol][0].notNil)
		{
			server.sendMsg(\n_set, nodes[player.asSymbol][0], \free, 0);
			"off".postln;
			// this.release;
		} {
			// Jouer une nouvelle synthèse
			server.sendMsg(\s_new, \Play_slice_ambi, nodes[player.asSymbol][1], 0, audio_group[player].nodeID, \startsamp, startsamp,  \buf, buffers[player.asSymbol].bufnum, \midicents, pitchshift, \out, audio_influencer_bus[player.asSymbol].index, \x, x, \y, y, \z, z);
			nodes[player.asSymbol][0] = nodes[player.asSymbol][1];
		}
	}

	play_slice_aoo {|player, startsamp, stopsamp = nil, pitchshift, cluster_lab|
		nodes[player.asSymbol][1] = server.nextNodeID+10;
		if(nodes[player.asSymbol][0].notNil)
		{
			server.sendMsg(\n_set, nodes[player.asSymbol][0], \free, 0);
			// ("stop "+previousNodeID).postln;
		};
		if(startsamp == "off" && nodes[player.asSymbol][0].notNil)
		{
			server.sendMsg(\n_set, nodes[player.asSymbol][0], \free, 0);
			"off".postln;
			// this.release;
		} {
			// Jouer une nouvelle synthèse
			server.sendMsg(\s_new, \Play_slice_aoo, nodes[player.asSymbol][1], 0, audio_group[player].nodeID, \startsamp, startsamp,  \buf, buffers[player.asSymbol].bufnum, \midicents, pitchshift, \outbus, audio_influencer_bus[player.asSymbol].index, \cluster, cluster_lab);
			nodes[player.asSymbol][0] = nodes[player.asSymbol][1];
		}
	}

	play_slice_aoo2 {|player, startsamp, stopsamp = nil, pitchshift, speakers, cluster_lab|
		nodes[player.asSymbol][1] = server.nextNodeID+10;
		if(nodes[player.asSymbol][0].notNil)
		{
			server.sendMsg(\n_set, nodes[player.asSymbol][0], \free, 0);
			// ("stop "+previousNodeID).postln;
		};
		if(startsamp == "off" && nodes[player.asSymbol][0].notNil)
		{
			server.sendMsg(\n_set, nodes[player.asSymbol][0], \free, 0);
			"off".postln;
			// this.release;
		} {
			// Jouer une nouvelle synthèse
			/*			server.sendMsg(\s_new, \Play_slice_aoo2, nodes[player.asSymbol][1], 0, audio_group[player].nodeID, \startsamp, startsamp,  \buf, buffers[player.asSymbol].bufnum, \outbus, audio_influencer_bus[player.asSymbol].index, \cluster, 0, \speakers, cluster_lab); //cluster_lab*/
			/*			server.sendBundle(nil, [\s_new, \Play_slice_aoo2, nodes[player.asSymbol][1], 0, audio_group[player].nodeID, \startsamp, startsamp,  \buf, buffers[player.asSymbol].bufnum, \outbus, audio_influencer_bus[player.asSymbol].index, \cluster, 0, \speakers, cluster_lab.flat].asOSCArgArray);*/
			server.sendBundle(nil, [\s_new, \Play_slice_aoo2, nodes[player.asSymbol][1], 0, audio_group[player].nodeID, \startsamp, startsamp,  \buf, buffers[player.asSymbol].bufnum, \midicents, pitchshift, \outbus, audio_influencer_bus[player.asSymbol].index, \speakers, speakers, \cluster, cluster_lab].asOSCArgArray);
			//cluster_lab
			/*			Synth(\Play_slice_aoo2, [ \startsamp, startsamp,  \buf, buffers[player.asSymbol].bufnum, \outbus, audio_influencer_bus[player.asSymbol].index, \cluster, 0, \speakers, cluster_lab], audio_group[player].nodeID);*/
			nodes[player.asSymbol][0] = nodes[player.asSymbol][1];
		}
	}

	play_midi {|player, notes|
		var result, pitch, vel, chan;
		pitch = notes[0];
		vel = notes[1];
		chan = notes[2];
		if(notes[1] != 0)
		{

			midi_instrument[player.asSymbol].midi.noteOn(chan , pitch, vel);
			midipitchclass[player.asSymbol].noteOn(pitch);
			result = midipitchclass[player.asSymbol].pitchClassValue;
			// player as influencer
			if(midi_influencer_dico[player.asSymbol].notNil)
			{
				midi_influencer_dico[player.asSymbol].collect({|dest|
					players_recv_osc[dest.asSymbol].sendMsg(dest, \influence, \melodic, \pitch, pitch);
					players_recv_osc[dest.asSymbol].sendMsg(dest, \influence, \harmonic, \chroma, *result);
					// 0.03.wait; // wait 30 ms for chords bang
					players_recv_osc[dest.asSymbol].sendMsg(dest, \bang);
				});
			}
		}
		{
			midi_instrument[player.asSymbol].midi.noteOff(chan , pitch, vel);
			midipitchclass[player.asSymbol].noteOff(pitch);
		}
	}

	midi_file_play {|player| // start runtime
		var lastTime = 0, result;
		midipitchclass.add(player ->  ComputeMemoryPitchClass.new); // midi chroma
		midi_file_play.add(player -> // play midi file
			{
				midi_influencer_events[player].do({ |evt|
					(evt[1] - lastTime).wait;
					lastTime = evt[1];
					switch(evt[2],
						\noteOn, { //"noteOn".postln ; evt.postln; player.postln;
							midi_instrument[player.asSymbol].midi.noteOn(0, evt[4] , evt[5]);
							midipitchclass[player.asSymbol].noteOn(evt[4]);
							result = midipitchclass[player.asSymbol].pitchClassValue;
							// midichroma.put(player, result); // put chroma result in dico
							// midichroma[player].postln;
							/// poner en un metodo
							midi_influencer_dico[player.asSymbol].collect({|dest|
								// ("INFLUENCER2 "+influencer).postln;
								players_recv_osc[dest.asSymbol].sendMsg(dest, \influence, \melodic, \pitch, evt[4]);
								players_recv_osc[dest.asSymbol].sendMsg(dest, \influence, \harmonic, \chroma, *result);

								// 0.03.wait; // wait 30 ms for chords bang
								players_recv_osc[dest.asSymbol].sendMsg(dest, \bang);
							});
						},
						\noteOff, { //"noteOff".postln ; evt.postln;  player.postln;
							midi_instrument[player.asSymbol].midi.noteOff(0, evt[4] , evt[5] );
							midipitchclass[player.asSymbol].noteOff(evt[4]);
						},
					);
				});
		}.fork);
	}
	midi_file_stop {|player|
		midipitchclass[player.asSymbol].routine.stop; // stop pitchclass computation
		midi_file_play[player].stop; // Stop runtime
		midi_instrument[player.asSymbol].midi.allNotesOff(1);
		vst_instrument[player.asSymbol].free;
	}
	midi_instrument_mute {|player, mute|
		vst_instrument[player.asSymbol].set(\bypass, mute);
	}

	/*	quickthresh {|pitch|
	pitch.postln;
	if(lock)
	{
	chord = chord.add(pitch);
	}
	{
	lock = true;
	chord = [pitch];
	{
	0.04.wait;
	chord.postln;
	lock = false;
	}.fork;
	};
	}*/

	///// GUI
	waveform {|player|
		buffers[player.asSymbol].postln;
		buffers.postln;
		FluidWaveform(buffers[player.asSymbol],bounds:Rect(0,0,1200,300));
	}

	/*		waveform_influencer {|player|
	buffers[player.asSymbol].postln;
	buffers.postln;
	FluidWaveform(buffers[player.asSymbol],bounds:Rect(0,0,1200,300));
	}*/
	// Influencer
	audio_influencer {|influencer, audio_input = \inputSound, input = nil, play = 0, loop = 0|
		// var newNodeID = server.nextNodeID;


		/*		server.sendMsg(\s_new, \Play_stereo, nodes[influencer.asSymbol], 0, audio_group[influencer].nodeID, \buf, audio_influencer_buffer[influencer.asSymbol].bufnum, \outbus, audio_influencer_bus[influencer.asSymbol].index, \loop, 1);
		*/
		// audio_influencer_dico.add(influencer.asSymbol -> []); // store influencer in dico

		// audio_descriptors.add(influencer.asSymbol -> [[0], [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]]); // store pitch and chroma
		case
		{audio_input == "off"}
		{
			"TOTOTOTOTOT".postln;
			// server.sendMsg(\n_free, nodes[influencer.asSymbol])
			server.sendMsg(\n_set, nodes[influencer.asSymbol], \free, 0);
			/*			server.sendMsg(\n_free, audio_descriptors_chroma[influencer]);
			server.sendMsg(\n_free, audio_descriptors_pitch[influencer]);
			server.sendMsg(\n_free, audio_descriptors_onset[influencer]);*/
			server.sendMsg(\n_free, audio_group[influencer].nodeID); // free descriptors group
			audio_descriptors.removeAt(influencer);
			audio_influencer_dico.removeAt(influencer);
			audio_influencer_info.removeAt(influencer);
			audio_influencer_buffer[influencer].free;
			audio_influencer_bus[influencer].free;
			// audio_group_lev[influencer].free;
			nodes[influencer.asSymbol].free;

			if(influencer_gui_dico.includesKey(influencer.asSymbol))
			{
				influencer_gui_dico.removeAt(influencer);
			};

			influencerl.do({|item, i|
				if(item.key == influencer.asSymbol)
				{
					influencerl.removeAt(i); //remove item in GUI Menu
				}
			});
			model_server.setParam(\audio_influencer, influencerl.collect({|x| x.key}).reverse);
		}
		{(audio_input == \soundFile )|| (audio_input == \audioIn) || (audio_input == \inputSound)}
		{
			("audio_influencer "++input).postln;
			if (audio_influencer_info.includesKey(influencer.asSymbol).not)
			{
				audio_influencer_info.add(influencer -> [0.15, input, \inputSound, true,  0.75, "Onset"]); // store influencer data onset_limiter, etc 0.75-> pitch quality]); // store influencer data onset_limiter, etc
				influencerl.insert(influencercount, influencer.asSymbol -> 0); // gui
				nodes.add(influencer.asSymbol -> server.nextNodeID); // init nodes_id
				audio_group.add(influencer -> Group.new);
				audio_influencer_bus.add(influencer.asSymbol -> Bus.audio(server, 2)); //stereo but be another

				/*lastPitchTimes.add(influencer.asSymbol -> nil); // for pitch filter
				lastAcceptedPitches.add(influencer.asSymbol -> 0); // for pitch filter*/

				this.audio_descriptors(influencer.asSymbol);

				model_server.setParam(\audio_influencer_SF, [influencer.asSymbol, nil]);

				model_server.setParam(\audio_influencer_loop,  [influencer, loop]);
				if(play == 0)
				{
					model_server.setParam(\audio_influencer_play,  [influencer, false]);
				}
				{
					model_server.setParam(\audio_influencer_play,   [influencer, true]);
				}
				{audio_input == \loop}
				{
					if(input == 0)
					{
						// server.sendMsg(\n_set, nodes[influencer.asSymbol], \loop, 0);
						model_server.setParam(\audio_influencer_loop,  [influencer, 0]);
					}
					{
						// server.sendMsg(\n_set, nodes[influencer.asSymbol], \loop, 1);
						model_server.setParam(\audio_influencer_loop,  [influencer, 1]);
					}
				};
				"ZERO".postln;
				audio_input.postln;
				if(audio_input == \inputSound)
				{
					"ZERO".postln;
					model_server.setParam(\audio_influencer_SF, [influencer.asSymbol, nil]);
				}
			};
			if (influencer_gui_dico.includesKey(influencer.asSymbol))
			{
				case
				{audio_input == \audioIn }
				{
					// influencer_gui_dico[influencer.asSymbol].select_input.value_(0);
					model_server.setParam(\audio_influencer_select_input,  0);
					influencer_gui_dico[influencer.asSymbol].input_bus_num.value_(input);
					influencer_gui_dico[influencer.asSymbol].select_input.value_(0);
					influencer_gui_dico[influencer.asSymbol].waveform_color(0);
				}
				{audio_input == \soundFile }
				{
					// influencer_gui_dico[influencer.asSymbol].select_input.value_(1)
					model_server.setParam(\audio_influencer_select_input,  1);
					influencer_gui_dico[influencer.asSymbol].select_input.value_(1);
					influencer_gui_dico[influencer.asSymbol].waveform_color(1);
				}
			};
			case
			{audio_input == \soundFile}
			{
				"SOUNDFILE".postln;
				if(nodes.includesKey(influencer.asSymbol))
				{
					server.sendMsg(\n_free, nodes[influencer.asSymbol]); // free Play or AudioIn synth
				};
				if(audio_influencer_buffer.includesKey(influencer.asSymbol))
				{
					nodes[influencer.asSymbol].free;
					audio_influencer_buffer[influencer.asSymbol].free;
					audio_influencer_buffer.add(influencer.asSymbol -> Buffer.read(server, input.asString)); // buffer and audio bus (send to analyse)
					model_server.setParam(\audio_influencer_SF, [influencer.asSymbol, input.asString]);
				}
				{
					audio_influencer_buffer.add(influencer.asSymbol -> Buffer.read(server, input.asString)); // buffer and audio bus (send to analyse)
					model_server.setParam(\audio_influencer_SF, [influencer.asSymbol, input.asString]);
					// this.audio_descriptors(influencer.asSymbol);
				};
				if (influencer_gui_dico.includesKey(influencer.asSymbol))
				{
					influencer_gui_dico[influencer.asSymbol].load_soundFile(input.asString);
				};
				audio_influencer_info.add(influencer -> [0.15, input, \soundFile, true, 0.75, "Onset"]); // store influencer data onset_limiter, etc 0.75-> pitch quality
				case
				{play == 1}
				{
					"PLAYPLAY".postln;

					server.sendMsg(\s_new, \Play_stereo, nodes[influencer.asSymbol], 0, audio_group[influencer].nodeID, \buf, audio_influencer_buffer[influencer.asSymbol].bufnum, \outbus, audio_influencer_bus[influencer.asSymbol].index, \loop, model_server[\params][\audio_influencer_loop][1], \index, nodes[influencer.asSymbol]);
				}
			}

			{audio_input == \audioIn}
			{
				"AUDIOINADIOIN".postln;
				audio_influencer_info.add(influencer -> [0.15, input, \audioIn, true, 0.75, "Onset"]); // store influencer data onset_limiter, etc 0.75-> pitch quality
				if(nodes.includesKey(influencer.asSymbol))
				{
					"AUDIOINADIOIN2222_NODE".postln;
					// nodes[influencer.asSymbol].postln;
					// nodes[influencer.asSymbol].nodeID.postln;
					server.sendMsg(\n_free, nodes[influencer.asSymbol]); // free Play or AudioIn synth

				};
				{0.01.wait;
					{
						"AUDIOINADIOIN3333".postln;
						server.sendMsg(\s_new, \AudioIn, nodes[influencer.asSymbol], 0, audio_group[influencer].nodeID, \input, input, \outbus, audio_influencer_bus[influencer.asSymbol].index, \index, nodes[influencer.asSymbol]);
						// this.audio_descriptors(influencer.asSymbol);
				}.defer}.fork;
			};
			{0.01.wait;
				{
					"AUDIOINADIOIN4444".postln;
					if(audio_group_lev.includesKey(influencer.asSymbol).not) // create a control level output
					{
						audio_group_lev.add(influencer -> Synth(\audioOut2, [\in, audio_influencer_bus[influencer.asSymbol].index, \index, audio_influencer_bus[influencer.asSymbol].index, \amp, -120], audio_group[influencer], addAction:'addToTail')); // Init audio output OFF -120dB (avoid feedback)
					};
			}.defer}.fork;
			// influencerl.collect({|x| x.key}).postln;
			"AUDIOINADIOIN5555".postln;
			model_server.setParam(\audio_influencer, influencerl.collect({|x| x.key}).reverse);
			/*}
			{
			"This Influencer already exist".postln;
			}*/
		}
		{audio_input == \stop}
		{
			"STOPPLAY".postln;
			model_server.setParam(\audio_influencer_play,  [influencer, false]);
			server.sendMsg(\n_set, nodes[influencer.asSymbol], \free, 0);
		}
		{audio_input == \play}
		{
			"PLAYPLAY".postln;
			model_server.setParam(\audio_influencer_play,  [influencer, true]);
			server.sendMsg(\n_set, nodes[influencer.asSymbol], \free, 0);
			server.sendMsg(\s_new, \Play_stereo, nodes[influencer.asSymbol], 0, audio_group[influencer].nodeID, \buf, audio_influencer_buffer[influencer.asSymbol].bufnum, \outbus, audio_influencer_bus[influencer.asSymbol].index, \loop, model_server[\params][\audio_influencer_loop][1], \index, nodes[influencer.asSymbol]);
		}
		{audio_input == \loop}
		{
			if(input == 0)
			{
				model_server.setParam(\audio_influencer_loop,  [influencer, 0]);
				server.sendMsg(\n_set, nodes[influencer.asSymbol], \loop, 0);
			}
			{
				model_server.setParam(\audio_influencer_loop,  [influencer, 1]);
				server.sendMsg(\n_set, nodes[influencer.asSymbol], \loop, 1);
			}
		}
	}
	/*	{audio_input == \audioIn}
	{
	if (audio_influencer_info.includesKey(influencer.asSymbol).not)
	{
	influencerl.insert(influencercount, influencer.asSymbol -> 0); // gui
	audio_group.add(influencer -> Group.new);
	audio_influencer_info.add(influencer -> [0.15, input, \audioIn, true]); // store influencer data onset_limiter, etc
	{0.1.wait;
	{
	audio_group_lev.add(influencer -> Synth(\audioOut8, target: audio_group[influencer], addAction:'addToTail'));
	}.defer}.fork;

	{0.1.wait;
	{
	nodes.add(influencer.asSymbol -> server.nextNodeID); // init nodes_id
	// audio_influencer_buffer.add(influencer.asSymbol -> [nil, Bus.audio(server)]); // buffer and audio bus (send to analyse)
	audio_influencer_bus.add(influencer.asSymbol -> Bus.audio(server));
	server.sendMsg(\s_new, \AudioIn, nodes[influencer.asSymbol], 0, 0, \input, input, \outbus, audio_influencer_bus[influencer.asSymbol].index, \index, nodes[influencer.asSymbol]);
	this.audio_descriptors(influencer.asSymbol);

	influencerl.collect({|x| x.key}).postln;
	model_server.setParam(\audio_influencer, influencerl.collect({|x| x.key}).reverse);

	}.defer}.fork;
	}
	{
	"This Influencer already exist".postln;
	}
	}*/

	// model_server.setParam(\audio_influencer, audio_influencer_info.keys.asArray.sort); //send audio_influencer list ordered, MVC
	/*"TOTOTOTOTOT".postln;
	influencerl.collect({|x| x.key}).postln;
	"TOTOTOTOTOT".postln;
	model_server.setParam(\audio_influencer, influencerl.collect({|x| x.key}));*/
	/*		if(influencer_gui_dico.includesKey(influencer.asSymbol).not)
	{
	// influencer_gui_dico.add(influencer.asSymbol -> nil); // empty element
	influencer_gui_dico.add(influencer.asSymbol -> Influencer_gui.new(influencer, 0, 0.1, audio_descriptors_onset, nodes, server, audio_influencer_info));
	}
	}*/

	influencer_gui {|influencer|
		var thresh;
		"ERROR".postln;
		influencer_gui_dico.add(influencer.asSymbol -> Influencer_gui.new(influencer, 0, 0.1, audio_descriptors_onset, nodes, server, audio_influencer_info, this));
	}

	player_gui {|player|
		^player_gui_dico.add(player.asSymbol -> Player_gui.new(player, 0, 0.1, audio_descriptors_onset, nodes, server, this));
	}

	player_open_gui {|player|
		player_gui_dico[player].front;
	}
	player_gui_sf {|player, sf_path, outnode, num_segs, onset_descriptor|
		player_gui_dico[player].load_soundFile(sf_path, outnode, num_segs, onset_descriptor);
	}
	setmenu {|player, item, corpus|
		player_gui_dico[player].setmenuitem(item, corpus);
		"IIIIIIIIIIIIIIIIIIIIIIIIIII".postln;
	}
	flucoma_labels  {|player, corpus|
		var ds_redux_file, audio_buffer, index_buffer, labels;
		var outbus = audio_influencer_bus[player.asSymbol].index;
		// corpus_name = (corpus.asString.drop(4));
		// "flucoma_path".postln;
		// (corpus_path++corpus_name++"_data_set_redux.json").postln;
		labels_dico.add(player.asSymbol -> FluidLabelSet(server));

		audio_buffer = buffers[player.asSymbol].bufnum;
		index_buffer = Buffer.read(server, corpus_path++corpus++"_slices.aif");
		ds_redux_file =  FluidDataSet(server).read(corpus_path++corpus++"_data_set_redux.json");
		labels = labels_dico[player.asSymbol]; // retrieve FluidLabelSet
		FluidKMeans(server,4).fitPredict(ds_redux_file, labels); // number clusters 4

		"labels".postln;
		// labels_dico[player.asSymbol].postln;

		flucoma_plotter_dico.add(player.asSymbol -> Plotter_clusters_gui.new(player, audio_buffer, index_buffer.bufnum, outbus, ds_redux_file, labels, server, this))
	}

	/*	flucoma_plotter  {|player, corpus|
	var ds_redux_file, audio_buffer, index_buffer, labels;
	var outbus = audio_influencer_bus[player.asSymbol].index;
	// corpus_name = (corpus.asString.drop(4));
	// "flucoma_path".postln;
	// (corpus_path++corpus_name++"_data_set_redux.json").postln;
	// labels = FluidLabelSet(server);
	labels = labels_dico[player.asSymbol]; // retrieve FluidLabelSet
	audio_buffer = buffers[player.asSymbol].bufnum;
	index_buffer = Buffer.read(server, corpus_path++corpus++"_slices.aif");
	ds_redux_file =  FluidDataSet(server).read(corpus_path++corpus++"_data_set_redux.json");
	// FluidKMeans(server,4).fitPredict(ds_redux_file, labels);

	flucoma_plotter_dico.add(player.asSymbol -> Plotter_clusters_gui.new(player, audio_buffer, index_buffer.bufnum, outbus, ds_redux_file, labels, server, this))
	}*/

	output  {|player, item, offset_out, amp|
		// var nodeID = audio_group_lev[player].nodeID;
		item.postln;
		// outputSelect = item;
		case
		{item == "mono"}
		{
			audio_group_lev[player.asSymbol] = Synth(\audioOut1, [\in, audio_influencer_bus[player.asSymbol].index, \amp, amp, \out, offset_out], audio_group_lev[player], \addReplace);
			player_output[player.asSymbol] = "mono";
/*			if(hoa_decoder.notNil) // free HOA general decoder if exist
			{
				hoa_decoder.free;
			};*/
		}
		{item == "stereo"}
		{
			audio_group_lev[player.asSymbol] = Synth(\audioOut2, [\in, audio_influencer_bus[player.asSymbol].index, \amp, amp, \out, offset_out], audio_group_lev[player], \addReplace);
/*			if(hoa_decoder.notNil) // free HOA general decoder if exist
			{
				hoa_decoder.free;
			};*/
			player_output[player.asSymbol] = "stereo";
		}
		{item == "multichannel"}
		{
			audio_group_lev[player.asSymbol] = Synth(\audioOut16, [\in, audio_influencer_bus[player.asSymbol].index, \amp, amp, \out, offset_out], audio_group_lev[player], \addReplace);

/*			if(hoa_decoder.notNil) // free HOA general decoder if exist
			{
				hoa_decoder.free;
			};*/
			player_output[player.asSymbol] = "multichannel";
		}
		{item == "ambisonic"}
		{
			// audio_group_lev[player.asSymbol] = Synth(\HOA_Studio1_Dec3, [\in, audio_influencer_bus[player.asSymbol].index, \amp, amp, \out, offset_out], audio_group_lev[player], \addReplace);
			// audio_group_lev[player.asSymbol] = Synth(\HOA_Hexa_Dec4, [\in, audio_influencer_bus[player.asSymbol].index, \amp, amp, \out, offset_out], audio_group_lev[player], \addReplace);
			// \HOA_Dec_ESPRO75_Rec
			// \HOADecSTUDIO5_7
	/*		audio_group_lev[player.asSymbol] = Synth( \HOA_Dec_ESPRO75_Rec, [\in, audio_influencer_bus[player.asSymbol].index, \amp, amp, \out, offset_out], audio_group_lev[player], \addReplace);*/

			if(hoa_decoder.isNil) // load decoder only if "ambisonic"
			{
				// hoa_decoder = Synth(\HOA_Studio1_Dec4, [\in, audio_to_decoder_bus.index, \out, offset_out], addAction:'addToTail');
				hoa_decoder = Synth(\HOA_Dec_MSH3, [\in, audio_to_decoder_bus.index, \out, offset_out], addAction:'addToTail');
			};
			// audio_group_lev[player.asSymbol] = Synth(\HOA_Out_4, [\in, audio_influencer_bus[player.asSymbol].index, \globTBus, audio_to_decoder_bus.index], audio_group_lev[player.asSymbol], \addReplace);
			audio_group_lev[player.asSymbol] = Synth(\HOA_Out_3, [\in, audio_influencer_bus[player.asSymbol].index, \globTBus, audio_to_decoder_bus.index, \amp, amp], audio_group_lev[player.asSymbol], \addReplace);

			player_output[player.asSymbol] = "ambisonic";
		}
		{item == "aoo"}
		{
			// audio_group_lev[player.asSymbol] = Synth(\audioOut24, [\in, audio_influencer_bus[player.asSymbol].index, \amp, amp, \out, offset_out], audio_group_lev[player], \addReplace);
			audio_group_lev[player.asSymbol] = Synth(\audioOut12, [\in, audio_influencer_bus[player.asSymbol].index, \amp, amp, \out, offset_out], audio_group_lev[player], \addReplace);
			player_output[player.asSymbol] = "aoo";
		}
		/*		out_nodeID = audio_group_lev[player].nodeID;
		out_nodeID.postln;*/
	}

	audio_descriptors {|influencer|

		audio_influencer_dico.add(influencer.asSymbol -> []); // store influencer in dico
		audio_descriptors.add(influencer.asSymbol -> [[0], [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0], [0,0,0,0,0,0,0,0,0,0,0,0,0,0]]); // store pitch, chroma and mfcc

		audio_descriptors_mfcc.add(influencer -> server.nextNodeID); // init nodes_id
		server.sendMsg(\s_new, \Fluid_MFCC_So, audio_descriptors_mfcc[influencer], 1, audio_group[influencer].nodeID, \in, audio_influencer_bus[influencer.asSymbol].index, \id, audio_descriptors_mfcc[influencer]); // id = node_id

		// audio_descriptors_synth.add(influencer++"chroma".asSymbol -> server.nextNodeID); // init nodes_id
		audio_descriptors_chroma.add(influencer -> server.nextNodeID); // init nodes_id
		server.sendMsg(\s_new, \Fluid_Chroma_So, audio_descriptors_chroma[influencer], 1, audio_group[influencer].nodeID, \in, audio_influencer_bus[influencer.asSymbol].index, \id, audio_descriptors_chroma[influencer]); // id = node_id

		audio_descriptors_pitch.add(influencer-> server.nextNodeID); // init nodes_id
		server.sendMsg(\s_new, \Fluid_Pitch_So, audio_descriptors_pitch[influencer], 1,  audio_group[influencer].nodeID, \in, audio_influencer_bus[influencer.asSymbol].index, \id, audio_descriptors_pitch[influencer]);

		audio_descriptors_onset.add(influencer -> server.nextNodeID); // init nodes_id

		/*		if(players_info[influencer.asSymbol].notNil) // registre onset node
		{
		players_info[influencer.asSymbol][3] = audio_descriptors_onset[influencer];
		};		*/

		server.sendMsg(\s_new, \Fluid_Onset_So, audio_descriptors_onset[influencer], 1,  audio_group[influencer].nodeID, \in, audio_influencer_bus[influencer.asSymbol].index, \id, audio_descriptors_onset[influencer]);

		/*		/// SendRplay OSC descriptors
		OSCdef(\chroma++influencer.asSymbol, {|msg|
		// msg[3..].postln;
		if(msg[2] == audio_descriptors_synth[influencer++"chroma"]) // if sendreplay id == node_id
		{
		audio_descriptors[influencer.asSymbol][1] = msg[3..];
		}
		// ~chroma = msg[3..];
		}, '/chroma');

		OSCdef(\pitch++influencer.asSymbol, {|msg|
		// ("pitchhhh"+msg).postln;
		// ~pitch = msg[3..];
		if(msg[2] == audio_descriptors_synth[influencer++"pitch"])
		{
		audio_descriptors[influencer.asSymbol][0] = msg[3..];
		}
		}, '/fluid_pitch');

		OSCdef(\onset++influencer.asSymbol, {|msg|
		var chroma_array, pitch_a;
		("INFLUENCER "+influencer).postln;
		msg.postln;
		audio_influencer_dico.postln;
		("INFLUENCER "+influencer).postln;
		if(msg[2] == audio_descriptors_synth[influencer++"onset"])
		{
		chroma_array = audio_descriptors[influencer.asSymbol][1];
		pitch_a = (audio_descriptors[influencer.asSymbol][0][0]).cpsmidi.round.asInteger;
		/*				("INFLUENCER "+influencer).postln;
		pitch_a.postln;
		chroma_array.postln;
		influencer.postln;
		("INFLUENCER "+influencer).postln;*/
		// Aqui quedé
		/*				destination.asArray.collect({|dest|
		players_recv_osc[dest.asSymbol].sendMsg(dest.asSymbol, \influence, \melodic, \pitch, pitch_a);
		players_recv_osc[dest.asSymbol].sendMsg(dest.asSymbol, \influence, \harmonic, \chroma, *chroma_array);
		players_recv_osc[dest.asSymbol].sendMsg(dest.asSymbol, \bang);
		});*/
		audio_influencer_dico[influencer.asSymbol].collect({|dest|
		// ("INFLUENCER2 "+influencer).postln;
		players_recv_osc[dest.asSymbol].sendMsg(dest, \influence, \melodic, \pitch, pitch_a);
		players_recv_osc[dest.asSymbol].sendMsg(dest, \influence, \harmonic, \chroma, *chroma_array);
		players_recv_osc[dest.asSymbol].sendMsg(dest, \bang);
		});
		}

		}, '/onsetdetect');*/
	}

	onset_params {|influencer, params|
		server.sendMsg(\n_set, audio_descriptors_onset[influencer], *params);
	}

	connection {|influencer, destination|
		if(audio_influencer_dico.includesKey(influencer)) // if audio influencer
		{
			if(audio_influencer_dico[influencer.asSymbol].includes(destination))
			{
				("connection "++influencer++" -> "++destination++" already exist ").postln;
			}
			{
				audio_influencer_dico[influencer.asSymbol] = audio_influencer_dico[influencer.asSymbol].add(destination).flat;

				// audio_influencer_dico.postln;
				"player_GUI".postln;
				player_gui_dico[destination.asSymbol].postln;
				players[destination.asSymbol].postln;
				audio_influencer_dico[influencer.asSymbol].postln;
				(players[destination.asSymbol].drop(1).asArray).postln;
				"player_destination".postln;
				destination.postln;
				"influencer".postln;
				influencer.postln;
				"pre_players_dico".postln;
				players.postln;
				"list_content".postln;
				players[destination].postln;
				"list_class".postln;
				players[destination].class.postln;
				"list_size".postln;
				players[destination].size.postln;
				players[destination].add(influencer); // Add influencer by in player dico
				"post_players_dico".postln;
				players.postln;
				player_gui_dico[destination.asSymbol].influence_source.items_(players[destination.asSymbol].drop(1).asArray); // add influencer to player gui
			};
		}
		{
			if(players.includesKey(influencer)) // player as infuencer
			{
				players[influencer].postln;
				if(players[influencer][0] == \audio) // player as infuencer
				{
					"  PLAYER AS INFLUENCER "++players.postln;
					/*					audio_influencer_info.add(influencer -> [0.15, nil, \playerInfluencer, true]); // store influencer data onset_limiter, etc
					audio_influencer_info.postln;
					this.audio_descriptors(influencer); // instantiate descriptors*/
					audio_influencer_dico[influencer.asSymbol] = audio_influencer_dico[influencer.asSymbol].add(destination).flat; // add connetions in dico
					players[destination].add(influencer); // Add influencer by in player dico
					("connection "++influencer++" -> "++destination).postln;
				}
				{
					// ("this audio player does not exist "++destination).postln;
				}
			}
		};
		if(midi_influencer_dico.includesKey(influencer)) // if midi influencer
		{
			// "Midi influencer "++influencer.postln;
			if(midi_influencer_dico[influencer.asSymbol].includes(destination))
			{
				("connection "++influencer++" -> "++destination++" already exist ").postln;
			}
			{
				midi_influencer_dico[influencer.asSymbol] = midi_influencer_dico[influencer.asSymbol].add(destination).flat;
				// "player_destination".postln;
				// players[destination].postln;
				players[destination].add(influencer); // Add influencer by in player dico
				("connection "++influencer++" -> "++destination).postln;
			};
		}
		{
			if(players.includesKey(influencer)) // player as infuencer
			{
				if(players[influencer][0] == \midi) // player as infuencer
				{
					// "influencer  PLAYER1 "++players.postln;
					// this.audio_descriptors(influencer); // instantiate descriptors
					midi_influencer_dico[influencer.asSymbol] = midi_influencer_dico[influencer.asSymbol].add(destination).flat; // add connetions in dico
					players[destination].add(influencer); // Add influencer by in player dico
					("connection "++influencer++" -> "++destination++" already exist ").postln;
				}
				{
					// ("this midi player does not exist "++destination).postln;
				}
			}
		};
	}

	disconnection {|influencer, destination|
		destination.asArray.collect({|dest|
			if(audio_influencer_dico[influencer.asSymbol].notNil)
			{
				// audio_influencer_dico[influencer.asSymbol].removeAt(audio_influencer_dico[influencer.asSymbol].indexOf(dest));
				audio_influencer_dico[influencer].remove(dest);
				players[dest].remove(influencer); // Remove influencer by in player dico

				("disconnection "++influencer++" -> "++destination).postln;
			};
			if(midi_influencer_dico[influencer.asSymbol].notNil)
			{
				// midi_influencer_dico[influencer.asSymbol].removeAt(midi_influencer_dico[influencer.asSymbol].indexOf(dest));
				midi_influencer_dico[influencer].remove(dest);
				("disconnection "++influencer++" -> "++destination).postln;
			};
		});
	}
	get_influencers {
		("audio_influencers "++audio_influencer_dico).postln;
		("midi_influencers "++midi_influencer_dico).postln;
	}

	get_players {
		("players "++players).postln;
	}
	influencer_delete_agent {|influencer, destination|
		// unregister OSCdef
		/*		OSCdef(\chroma++influencer.asSymbol).free;
		OSCdef(\pitch++influencer.asSymbol).free;
		OSCdef(\onset++influencer.asSymbol).free;*/
		// remove from dictionary
		/*		server.sendMsg(\n_free, audio_descriptors_chroma[influencer]);
		server.sendMsg(\n_free, audio_descriptors_pitch[influencer]);
		server.sendMsg(\n_free, audio_descriptors_onset[influencer]);*/
		audio_descriptors_chroma.removeAt(influencer);
		audio_descriptors_pitch.removeAt(influencer);
		audio_descriptors_onset.removeAt(influencer);
	}

	corpus_buider {|analyse_file, seg_mode = \onset, min_seg_dur = 0.05, peak_window = 0.40, peak_thresh = 0.07, init_bpm = 120, interval_dur = 0.5, hop_length = 512, overwrite = 1|

		// analyse file on server
		osc_to_server.sendMsg('/somax', 'build_corpus', corpus_path++analyse_file, 'overwrite=', overwrite, 'corpus_name=', analyse_file.findRegexp("^(.*)\\.")[1][1], 'output_folder=', corpus_path, 'builder_address=', "1014_corpusbuilder", 'segmentation_mode=', seg_mode, 'max_size_s=', \None, 'off_threshold_db=', \None, 'discard_by_mean=', 0, 'pick_peak_delta_gain=', peak_thresh, 'segmentation_interval_s=', interval_dur, 'estimated_initial_bpm=', init_bpm, 'hop_length=', hop_length, 'min_interval_s=', min_seg_dur, 'copy_resources=', 0, 'pick_peak_pre_mean_s=', peak_window, 'pick_peak_pre_max_s=', peak_window, 'pick_peak_post_mean_s=', peak_window, 'pick_peak_post_max_s=', peak_window);

	}

	corpus_test_buider {|analyse_file, seg_mode = \onset, min_seg_dur = 0.05, peak_window = 0.40, peak_thresh = 0.07, init_bpm = 120, interval_dur = 0.5, hop_length = 512, overwrite = 1|
		ana_file_name = analyse_file;
		test_buid_buf = Buffer.read(server, corpus_path++analyse_file);
		// analyse file on server
		osc_to_server.sendMsg('/somax','test_audio_segmentation', corpus_path++analyse_file, 'segmentation_mode=', seg_mode, 'max_size_s=','None','off_threshold_db=',\None, 'discard_by_mean=', 0,'pick_peak_delta_gain=', peak_thresh, 'segmentation_interval_s=', interval_dur, 'estimated_initial_bpm=', init_bpm, 'hop_length=', hop_length, 'min_interval_s=', min_seg_dur, 'copy_resources=',0, 'pick_peak_pre_mean_s=', peak_window, 'pick_peak_pre_max_s=', peak_window, 'pick_peak_post_mean_s=', peak_window, 'pick_peak_post_max_s=', peak_window, 'builder_address=', "1014_corpusbuilder");

	}

	display_test_corpus {
		displayonsets = FluidWaveform(test_buid_buf, buf_onsets);

	}

	buf_onsets_plot {
		buf_onsets.plot;
		buf_onsets.getn(0, 10);
		index_list.postln;
	}

	midi_influencer {|influencer, midi_input = \midiFile, midi_path, instr = nil|
		if(midi_influencer_dico.getPairs.isNil) // Start Midi client
		{
			MIDIClient.init;
			MIDIIn.connectAll;
		};
		midi_influencer_dico.add(influencer.asSymbol -> []); // store influencer in dico

		if (instr.notNil)
		{
			this.load_midi_instrument(influencer, instr); // load Midi virtual instrument
		};
		case
		{midi_input == "off"}
		{
			if(midi_influencer_player[influencer].notNil) // Stop midi file player
			{
				this.midi_file_stop(influencer);
			}
			{
				midipitchclass[influencer.asSymbol].routine.stop; // stop pitchclass computation
				midi_functions[influencer++"noteon"].free; // free Midi Functions
				midi_functions[influencer++"noteoff"].free; // free Midi Functions
				vst_instrument[influencer.asSymbol].free; // free VST instrument
			};
			midi_influencer_dico.removeAt(influencer.asSymbol); // remove from dico
		}

		/*
		{midi_input == "on"}
		{
		server.sendMsg(\s_new, \Play_stereo, nodes[influencer.asSymbol], 0, audio_group[influencer].nodeID, \buf, audio_influencer_buffer[influencer.asSymbol].bufnum, \outbus, audio_influencer_bus[influencer.asSymbol].index, \loop, 1);
		}*/

		{midi_input == \midiFile}
		{
			("midi_influencer ").postln;

			midi_influencer_player.add(influencer ->  SimpleMIDIFile.read(midi_path));
			midi_influencer_player[influencer].timeMode = \seconds;
			midi_influencer_events.add(influencer ->  midi_influencer_player[influencer].midiEvents);
			this.midi_file_play(influencer); // start runtime

			// this.audio_descriptors(influencer.asSymbol);
		}
		{midi_input == \midiIn}
		{
			var result;
			midipitchclass.add(influencer ->  ComputeMemoryPitchClass.new); // midi chroma

			midi_functions.add(influencer++"noteon" -> MIDIFunc.noteOn({ |vel, pitch|
				midi_instrument[influencer.asSymbol].midi.noteOn(0, pitch, vel);
				midipitchclass[influencer.asSymbol].noteOn(pitch);
				result = midipitchclass[influencer.asSymbol].pitchClassValue;
				result.postln;
				midi_influencer_dico[influencer.asSymbol].collect({|dest|
					// ("INFLUENCER2 "+influencer).postln;
					players_recv_osc[dest.asSymbol].sendMsg(dest, \influence, \melodic, \pitch, pitch);
					players_recv_osc[dest.asSymbol].sendMsg(dest, \influence, \harmonic, \chroma, *result);

					// 0.03.wait; // wait 30 ms for chords bang
					players_recv_osc[dest.asSymbol].sendMsg(dest, \bang);
				});
			});
			);
			midi_functions.add(influencer++"noteoff" -> MIDIFunc.noteOff({ |vel, pitch|
				midi_instrument[influencer.asSymbol].midi.noteOff(0, pitch, vel);
				midipitchclass[influencer.asSymbol].noteOff(pitch);
			});
			);
		};
	}
	vst_master_end {|player, vst|
		iemReverb.add(player -> VSTPluginController(Synth(\ambiFX, [\bus, audio_influencer_bus[player], \out, audio_influencer_bus[player]],  audio_group_lev[player], \addBefore)).open(vst));
		{0.01.wait;{
			iemReverb.editor;
		}.defer}.fork;
	}
	vst_master_end_off {|player, vst|
		iemReverb[player].free;
	}
	vst_master_eq {|player, vst|
		iemEQ.add(player -> VSTPluginController(Synth(\ambiFX, [\bus, audio_influencer_bus[player], \out, audio_influencer_bus[player]],  audio_group_lev[player], \addBefore)).open(vst));
		{0.01.wait;{
			iemEQ.editor;
		}.defer}.fork;
	}
	vst_master_eq_off {|player, vst|
		iemEQ[player].free;
	}

	init_osc_control {

		if(osc_control.notNil) { osc_control.free };

		osc_control = OSCdef(\somax_control, { |msg|
			var arg0 = msg[1].asString;  // "create_agent" ou "Agent_1/enabled" ou "AudioInfluencer_1/amp_in"
			var val  = msg[2];           // valeur principale
			var val2 = msg[3];           // valeur secondaire (optionnelle)

			// ── Format avec "/" : paramètre d'un agent ou influencer ──
			if(arg0.contains("/")) {
				var parts = arg0.split($/);
				var name  = parts[0].asSymbol;
				var param = parts[1].asString;

				// ── Agent ──
				if(players.includesKey(name)) {
					var player = name;
					{
						switch(param,

							// ── Fenêtres ──
							"win", {
								if(val.asInteger == 1) {
									player_gui_dico[player].win.front;
								} {
									player_gui_dico[player].win.close;
								};
							},
							"plotter", {
								if(flucoma_plotter_dico[player].notNil) {
									if(val.asInteger == 1) {
										flucoma_plotter_dico[player].win.front;
									} {
										flucoma_plotter_dico[player].win.close;
									};
								};
							},

							// ── Paramètres GUI + OSC Python ──
							"enabled", {
								this.set_params(player, \enabled, val.asInteger);
							},
							"continuity", {
								this.set_params(player, \continuity, val.asFloat);
								player_gui_dico[player].continuitySlider.value_(val.asFloat);
							},
							"quality", {
								this.set_params(player, \quality, val.asFloat);
								player_gui_dico[player].qualitySlider.value_(val.asFloat);
							},
							"probability", {
								this.set_params(player, \outputprobability, val.asFloat);
								player_gui_dico[player].probSlider.value_(val.asFloat);
							},
							"amp", {
								this.level(player, val.asFloat);
								player_gui_dico[player].slider.value_(
									player_gui_dico[player].dbspec.unmap(val.asFloat)
								);
							},
							"time_stretch", {
								this.set_time_stretch(player, val.asFloat);
								player_gui_dico[player].time_stretch_slider.value_(val.asFloat);
							},
							"sparse", {
								this.set_params(player, \sparse, val.asInteger);
								player_gui_dico[player].sparseButton.value_(val.asInteger);
							},
							"cut", {
								this.set_params(player, \cut, val.asInteger);
								player_gui_dico[player].cutButton.value_(val.asInteger);
							},
							"playing_mode", {
								this.playing_mode(player, val.asInteger);
							},
							"beat_align", {
								this.set_params(player, \beat_align, val.asInteger);
								player_gui_dico[player].beatAlignButton.value_(val.asInteger);
							},
							"jump", {
								this.set_params(player, \jump, val.asInteger);
								player_gui_dico[player].state_slider.value_(val.asInteger);
							},
							"weights", {
								var w = msg[2..7].collect(_.asFloat);
								this.set_params(player, \weights, w);
								player_gui_dico[player].weight_multislider.values_(w);
							},
							"timeout", {
								var v = val.asFloat;
								if(v == 0) {
									players_recv_osc[player.asSymbol].sendMsg(player.asSymbol, \set_timeout, \None);
									defer {
										player_gui_dico[player].timeoutToggle.value_(0);
										player_gui_dico[player].timeoutUpdateContent.(0);
									};
								} {
									this.set_time_out(player, v);
									defer {
										player_gui_dico[player].timeoutToggle.value_(1);
										player_gui_dico[player].timeoutVal = v;
										player_gui_dico[player].timeoutUpdateContent.(1);
									};
								};
							},
							"output_mode", {
								var modeName = val.asString;
								var idx = player_gui_dico[player].outputMenu.items.detectIndex({ |item|
									item.asString == modeName
								});
								if(idx.notNil) {
									player_gui_dico[player].currentModeIndex = idx;
									player_gui_dico[player].outputMenu.value_(idx);
									player_gui_dico[player].setupMode.(idx);
									this.output(player, modeName, 0,
										player_gui_dico[player].dbspec.map(player_gui_dico[player].currentVal));
								} {
									("output_mode: mode not found -> " ++ modeName).postln;
								};
							},

							// ── OSC Python seulement ──
							"add_transform", {
								this.add_transform(player, val.asInteger);
							},
							"remove_transform", {
								this.remove_transform(player, val.asInteger);
							},
							"ngram_self", {
								this.ngram_size(player, \self, val.asInteger);
							},
							"ngram_melodic", {
								this.ngram_size(player, \melodic, val.asInteger);
							},
							"ngram_harmonic", {
								this.ngram_size(player, \harmonic, val.asInteger);
							},
							"ngram_mfcc", {
								this.ngram_size(player, \mfcc, val.asInteger);
							},
							"ngram_selfharmonic", {
								this.ngram_size(player, \selfharmonic, val.asInteger);
							},
							"ngram_selfmfcc", {
								this.ngram_size(player, \selfmfcc, val.asInteger);
							},

							// ── Corpus et presets ──
							"load_corpus", {
								var corpusName = val.asString;
								var gui = player_gui_dico[player];
								this.load_corpus(player, corpusName, nil, nil, nil, false);
								if(gui.notNil && gui.corpus_menu.items.notNil) {
									var idx = gui.corpus_menu.items.detectIndex({ |item|
										item.asString.drop(4) == corpusName
									});
									if(idx.notNil) {
										defer { gui.corpus_menu.value_(idx) };
									};
								};
							},
							"load_preset", {
								this.load_preset(val.asString, player);
							},

							// ── Connections player -> player ──
							"connect_player", {
								var agentName = val.asString.asSymbol;
								if(players.includesKey(agentName)) {
									this.connection(player, agentName);
									if(player_gui_dico[agentName].notNil) {
										player_gui_dico[agentName].influence_source.items_(
											players[agentName].drop(1).asArray
										);
									};
								};
							},
							"disconnect_player", {
								var agentName = val.asString.asSymbol;
								if(players.includesKey(agentName)) {
									this.disconnection(player, agentName);
									if(player_gui_dico[agentName].notNil) {
										player_gui_dico[agentName].influence_source.items_(
											players[agentName].drop(1).asArray
										);
									};
								};
							},

							// ── Fallback → envoie directement au serveur Python ──
							{
								this.set_params(player, param.asSymbol, val);
							}
						);
					}.defer;

					// ── Influencer ──
				} {
					if(influencer_gui_dico.includesKey(name)) {
						var gui = influencer_gui_dico[name];
						var v;
						{
							switch(param,
								"win", {
									if(val.asInteger == 1) {
										gui.win.front;
									} {
										gui.win.close;
									};
								},
								"amp_in", {
									v = val.asFloat;  // -30 dB
									gui.sliderIn.value_(gui.dbspec.unmap(v));  // unmap dB → 0-1 pour le slider
									server.sendMsg(\n_set, nodes[name], *[\amp, v.dbamp]);  // dB → amplitude linéaire pour SC server
								},
								"amp_out", {
									v = val.asFloat;  // 6 dB
									gui.sliderOut.value_(gui.dbspec.unmap(v));  // unmap dB → 0-1 pour le slider
									server.sendMsg(\n_set, audio_group_lev[name].nodeID, *[\amp, v.dbamp]);  // dB → amplitude
								},
								"onset_threshold", {
									v = val.asFloat;
									gui.onsetThresholdSlider.value_(v);
									server.sendMsg(\n_set, audio_descriptors_onset[name], *[\threshold, v]);
								},
								"onset_limiter", {
									v = val.asFloat;
									gui.onsetLimiterSlider.value_(v);
									audio_influencer_info[name][0] = v;
								},
								"pitch_quality", {
									v = val.asFloat;
									gui.pitchQualitySlider.value_(v);
									audio_influencer_info[name][4] = v;
								},
								"enabled", {
									v = val.asInteger;
									gui.enableButton.value_(v);
								},
								"onset_type", {
									var v = val.asString;
									var idx = gui.onsetTypeMenu.items.detectIndex({ |item|
										item.asString == v
									});
									if(idx.notNil) {
										gui.onsetTypeMenu.value_(idx);
										audio_influencer_info[name][5] = v;
									} {
										("onset_type: type inconnu -> " ++ v).postln;
									};
								},
								{ ("OSC influencer: param inconnu -> " ++ param).postln }
							);
						}.defer;
					} {
						("OSC: name not found -> " ++ name).postln;
					};
				};

			} {

				// ── Format sans "/" : commande globale ──
				var cmd  = arg0;
				var arg1 = if(val.notNil)  { val.asString.asSymbol  } { nil };
				var arg2 = if(val2.notNil) { val2.asString          } { nil };

				switch(cmd,

					// ── Agents ──
					"create_agent", {
						// /somax_cmd create_agent Agent_1 Chopin_nocturne
						defer { this.create_agent(arg1, arg2) };
					},
					"delete_agent", {
						// /somax_cmd delete_agent Agent_1
						if(players.includesKey(arg1)) {
							defer {
								if(player_gui_dico.includesKey(arg1)) {
									player_gui_dico[arg1].win.close;
								};
								if(flucoma_plotter_dico[arg1].notNil) {
									flucoma_plotter_dico[arg1].win.close;
								};
								this.delete_agent(arg1);
							};
						};
					},

					// ── Influencers ──
					"create_influencer", {
						// /somax_cmd create_influencer AudioInfluencer_1 0
						var busNum = if(arg2.notNil) { arg2.asInteger } { 0 };
						defer { this.audio_influencer(arg1, \audioIn, busNum) };
					},
					"delete_influencer", {
						// /somax_cmd delete_influencer AudioInfluencer_1
						defer {
							if(influencer_gui_dico.includesKey(arg1)) {
								influencer_gui_dico[arg1].win.close;
							};
							this.audio_influencer(arg1, "off");
						};
					},

					// ── Connections influencer -> agent ──
					"connect", {
						// /somax_cmd connect AudioInfluencer_1 Agent_1
						var agentName = arg2.asSymbol;
						if(players.includesKey(agentName)) {
							defer {
								this.connection(arg1, agentName);
								if(player_gui_dico[agentName].notNil) {
									player_gui_dico[agentName].influence_source.items_(
										players[agentName].drop(1).asArray
									);
								};
							};
						};
					},
					"disconnect", {
						// /somax_cmd disconnect AudioInfluencer_1 Agent_1
						var agentName = arg2.asSymbol;
						if(players.includesKey(agentName)) {
							defer {
								this.disconnection(arg1, agentName);
								if(player_gui_dico[agentName].notNil) {
									player_gui_dico[agentName].influence_source.items_(
										players[agentName].drop(1).asArray
									);
								};
							};
						};
					},

					// ── Transport ──
					"run", {
						// /somax_cmd run
						defer { this.run };
					},
					"stop", {
						// /somax_cmd stop
						defer { this.stop };
					},

					// ── Presets globaux ──
					"load_preset_all", {
						// /somax_cmd load_preset_all mypreset
						players.keysDo { |pl|
							defer { this.load_preset(arg1.asString, pl) };
						};
					},

					// ── Fallback ──
					{
						("OSC: commande inconnue -> " ++ cmd).postln;
					}
				);
			};

		}, '/somax_cmd', nil, 3344);
	}



	ambi_position { |distribution|
		var spread = 0.3;
		var x = 0, y = 0, z = 0;
		var angle, elev;

		switch(distribution,
			\random, {
				x = rrand(-1.0, 1.0);
				y = rrand(-1.0, 1.0);
				z = rrand(-1.0, 1.0);
			},
			\front, {
				x = rrand(0.5, 1.0);
				y = rrand(spread.neg, spread);
				z = rrand(spread.neg, spread);
			},
			\back, {
				x = rrand(-1.0, -0.5);
				y = rrand(spread.neg, spread);
				z = rrand(spread.neg, spread);
			},
			\left, {
				x = rrand(spread.neg, spread);
				y = rrand(-1.0, -0.5);
				z = rrand(spread.neg, spread);
			},
			\right, {
				x = rrand(spread.neg, spread);
				y = rrand(0.5, 1.0);
				z = rrand(spread.neg, spread);
			},
			\up, {
				x = rrand(spread.neg, spread);
				y = rrand(spread.neg, spread);
				z = rrand(0.5, 1.0);
			},
			\down, {
				x = rrand(spread.neg, spread);
				y = rrand(spread.neg, spread);
				z = rrand(-1.0, -0.5);
			},
			\center, {
				x = rrand(spread.neg, spread);
				y = rrand(spread.neg, spread);
				z = rrand(spread.neg, spread);
			},
			\front_left, {
				x = rrand(0.4, 1.0);
				y = rrand(-1.0, -0.4);
				z = rrand(spread.neg, spread);
			},
			\front_right, {
				x = rrand(0.4, 1.0);
				y = rrand(0.4, 1.0);
				z = rrand(spread.neg, spread);
			},
			\back_left, {
				x = rrand(-1.0, -0.4);
				y = rrand(-1.0, -0.4);
				z = rrand(spread.neg, spread);
			},
			\back_right, {
				x = rrand(-1.0, -0.4);
				y = rrand(0.4, 1.0);
				z = rrand(spread.neg, spread);
			},
			\circle_h, {
				angle = rrand(0.0, 2pi);
				x = cos(angle) * rrand(0.6, 1.0);
				y = sin(angle) * rrand(0.6, 1.0);
				z = rrand(spread.neg, spread);
			},
			\dome, {
				angle = rrand(0.0, 2pi);
				elev  = rrand(0.3, 1.0);
				x = cos(angle) * (1 - elev.abs);
				y = sin(angle) * (1 - elev.abs);
				z = elev;
			},
			\rotating, {
				angle = (thisThread.seconds * 0.5) % (2pi);
				x = cos(angle) * rrand(0.7, 1.0);
				y = sin(angle) * rrand(0.7, 1.0);
				z = rrand(spread.neg, spread);
			},
			{
				x = rrand(-1.0, 1.0);
				y = rrand(-1.0, 1.0);
				z = rrand(-1.0, 1.0);
			}
		);
		^[x, y, z]
	}

	set_ambi_distribution { |player, distribution|
		player_ambi_distribution[player.asSymbol] = distribution;
		("Ambi distribution: " ++ player ++ " -> " ++ distribution).postln;
	}

	plot {|agent, ds, action|

		var kdtree = FluidKDTree(server);
		var buf_2d = Buffer.alloc(server,2);
		var scaler = FluidNormalize(server);
		var ds_norm = FluidDataSet(server);

		FluidNormalize(server).fitTransform(ds,ds_norm);
		kdtree.fit(ds_norm);
		ds_norm.dump({
			arg dict;
			var previous, fp, cluster_lab;
			fork({
				~fp = FluidPlotter(bounds:Rect(0,0,800,800),dict:dict,mouseMoveAction:{
					arg view, x, y;
					buf_2d.setn(0,[x,y]); // load it into a buffer so that...
					kdtree.kNearest(buf_2d,1,{ // it can be passed to the kdtree to find hte nearest neighbour, which is reported back...
						arg nearest; // here
						nearest.postln;
						if(previous != nearest,{ // only if it is a "new" nearest neighbour, should it make sound
							var index = nearest.asString.split($-)[1].asInteger; // peel off the index of the slice
							previous = nearest;
							index.postln;
							"nearest".postln;
							nearest.postln;
							labels.getLabel(nearest.asString, {|lab| cluster_lab = lab; });
							"nearest point is: %".format(nearest).postln;
							view.highlight_(nearest);
							/*						{
							var startPos = Index.kr(~indices,index); // look up the start position
							var dur_samps = Index.kr(~indices,index + 1) - startPos; // calculate the duration in samples

							// play the buffer starting from the start position
							var sig = PlayBuf.ar(2,~loader.buffer,BufRateScale.ir(~loader.buffer),startPos:startPos)[0];
							var dur_sec = min(dur_samps / BufSampleRate.ir(~loader.buffer),1);
							var env = EnvGen.kr(Env([0,1,1,0],[0.03,dur_sec-0.06,0.03]),doneAction:2);
							sig.dup * env;
							}.play;*/
							Synth(\Play_slice_cluster, [\buf, ~loader, \idx, ~indices, \index, index, \cluster, cluster_lab.asInt]);
							"cluster_lab "++cluster_lab.postln;
						});
					});
				});
				action.value;
			}, AppClock);
		});
		// this.plot_cluster();
	}

	plot_cluster {|player|
		var corp_anal;
		labels = FluidLabelSet(server);
		corp_anal = players_info[player][0]; // corpus name
		FluidKMeans(server,3).fitPredict(data_set_redux,labels); // try with a different number of clusters
		this.plot.(player, data_set_redux,{
			labels.dump{
				arg labelsdict;
				labelsdict.postln;
				~fp.categories_(labelsdict);
			}
		});
	}
}




/*a = ComputeMemoryPitchClass.new
a.noteOn(60)
a.noteOff(60)
a.reset
a.bang*/


ComputeMemoryPitchClass {
	// from Somax Max js code
	// global variables
	// v for variable
	var v_timeStep = 50; //ms
	var v_tau_up = 400; //ms
	var v_tau_down = 1000; //ms
	var v_p_max = 1.0;
	var v_threshold = 0.05;
	var v_m_0 = 0.5;
	var v_nbMaxHarmonics = 10;
	var v_decayParam = 0.5;
	var <pitchState;
	var <pitchValue;
	var <pitchClassValue;
	var k, indTmp;
	var <>routine;
	// pitchState = 0: nothing; 1: up; 2: down;

	*new {
		^super.newCopyArgs().init();
	}

	// initialize
	init {
		"pitchClass INIT".postln;
		pitchState = Array.fill(128, 0);
		pitchValue = Array.fill(128, 0.0);
		pitchClassValue = Array.fill(12, 0);
		this.reset();
		routine = Routine {
			loop {
				0.05.wait;
				this.bang;
			}
		}.play;
	}

	bang {
		this.updateMemoryPitchVector();
		this.updateMemoryPitchClassVector();
		// pitchClassValue.postln;
		^pitchClassValue;
	}

	noteOn{|pitch|
		pitchValue[pitch] = max(pitchValue[pitch], v_m_0);
		pitchState[pitch] = 1;

		//add harmonics or combination tones
		k=1;
		indTmp = pitch + round(12 * log(k+1)/log(2));
		while { (k <= v_nbMaxHarmonics) && (indTmp < 127) }
		{
			if (pitchValue[pitch]*v_decayParam > pitchValue[indTmp])
			{
				pitchValue[indTmp] = pitchValue[pitch]*pow(v_decayParam,k);
				pitchState[indTmp] = 2;
			};
			k = k+1;
			indTmp = pitch + round(12 *log(k+1)/log(2));

		};
	}

	noteOff{|pitch|
		pitchState[pitch] = 2;
	}

	reset{
		for (0, 127) {|i|
			pitchState[i] = 0;
			pitchValue[i] = 0.0;
		};
		for (0, 11) {|i|
			pitchClassValue[i] = 0.0;
		};
	}

	timeStep{|tStep|
		v_timeStep = tStep;
	}

	tau_up{|newTau|
		v_tau_up = newTau;
	}

	tau_down{|newTau|
		v_tau_down = newTau;
	}

	threshold{|newThsld|
		v_threshold = newThsld;
	}

	nbMaxHarmonics{|newNbHarmonics|
		v_nbMaxHarmonics = newNbHarmonics;
	}

	decay{|newDecay|
		v_decayParam = newDecay;
	}


	//  ------------------ private functions

	updateMemoryPitchClassVector{
		// var indTmp;
		for (0, 11){|i|
			pitchClassValue[i] = 0.0;
		};
		for (0, 127) {|i|
			/*			pitchClassValue.postln;
			pitchValue.postln;*/
			indTmp =  i%12;
			// pitchClassValue[indTmp].postln;
			pitchClassValue[indTmp] =  pitchClassValue[indTmp] + pitchValue[i];
		};
	}
	// 50,5 0,00125
	// updateMemoryPitchClassVector.local = 1;
	updateMemoryPitchVector{
		// var indTmp, k;
		for (0, 127) {|i|
			// pitchState[i].postln;
			if (pitchState[i] == 1)
			{
				pitchValue[i] = pitchValue[i] +( v_timeStep*((v_p_max - pitchValue[i])/v_tau_up));
			}
			{ if (pitchState[i] == 2)
				{ pitchValue[i] = pitchValue[i] - (v_timeStep*(pitchValue[i]/v_tau_down));
					// pitchValue.postln;
				}
			}
		};

		// look at harmonics
		for (0, 127) {|i|
			if (pitchState[i] == 1)
			{
				k=1;
				indTmp = i + round(12 *log(k+1)/log(2));
				while { (k <= v_nbMaxHarmonics) && (indTmp < 128) }
				{
					if (pitchState[indTmp] == 2)
					{
						pitchValue[indTmp] = max(pitchValue[indTmp], pitchValue[i] *pow(v_decayParam,k));
					};
					k = k+1;
					indTmp = i + round(12 *log(k+1)/log(2));
					/*					k.postln;
					indTmp.postln;
					pitchValue.postln;*/
				}
			}
		};

		for (0, 127) {|i|
			if ( (pitchState[i] == 2) && (pitchValue[i] < v_threshold ) )
			{
				pitchState[i] = 0;
				pitchValue[i]= 0.0;
			}
		}
	}
	// updateMemoryPitchVector.local = 1;

}



/*
(
m = SimpleMIDIFile( "/Users/josephfernandez/Documents/Siena/Max/Max-Ircam-Cours/patch_Max6Jours_old/MaxInteraction-Grégoire/1- Midi/Bach-gavotte.mid" ); // create empty file
m.init1( 3, 120, "4/4" );    // init for type 1 (multitrack); 3 tracks, 120bpm, 4/4 measures
m.timeMode = \seconds;  // change from default to something useful

((0,(1/8)..5)).do({ |starttime| // add random notes
m.addNote( 36 + 50.rand, 32 + 96.rand, starttime, [0.1,0.05].choose, 127, track: 1 )
});

((0,(1/4)..5)).do({ |starttime| // add random notes to next track
m.addNote( 36 + 50.rand, 64 + 64.rand, starttime, [0.1,0.025].choose, 127,
channel: 1,  // note: = midi channel 2
track: 2 )
});
)
m.midiEvents.dopostln; // all midi events
m.metaEvents.dopostln; // notice the incorrect 'endOfTrack' events for track 1 & 2;

m.adjustEndOfTrack;
m.metaEvents.dopostln; // try again

m.tempoMap; // get the tempo map ( [[ startTime, bpm ], [ etc.. ]] )
m.timeSignatures; // get the time signatures ( ( [[ startTime, a/b ], [ etc.. ]] )

m.p.play; // convert to Pattern and play
m.write; // now play the file in Quicktime, or open with another app
m.plot;  // uses ScaledUserView::

// read it
m = SimpleMIDIFile.read( "/Users/josephfernandez/Documents/Siena/Max/Max-Ircam-Cours/patch_Max6Jours_old/MaxInteraction-Grégoire/1- Midi/Bach-gavotte.mid" );

// play it (cmd-. to stop)
m.p.play; // takes a few seconds to start because this midifile starts with a rest
m.generatePatternSeqs

m.play
m.noteOnEvents()


f = SimpleMIDIFile.read( "/Users/josephfernandez/Documents/Siena/Max/Max-Ircam-Cours/patch_Max6Jours_old/MaxInteraction-Grégoire/1- Midi/Bach-gavotte.mid" );
x = f.p;

y = x.iter.all(()).postcs;
x.noteOn[1]

f.midiDeltaEvents
f.noteSustainEvents
f.shiftTime
f.midiDeltaEvents

f.noteEvents()
f.noteSustainEvents()
f.midiDeltaEvents()
f.length
f.trackNames
f.midiEvents
f.timeMode = \seconds;
//tests..
y[8].midinote;
y[8].delta;
y[5].delta;
y[8].vel_damp_;


(
SynthDef(\vartest, { |out, freq = 440, amp = 0.2, a = 0.01, r = 1|
// the EnvGen with doneAction: Done.freeSelf frees the synth automatically when done
Out.ar(out, Pulse.ar(freq, 0.2, EnvGen.kr(Env.perc(a, r, amp), doneAction: 2)));
}, variants: (alpha: [a: 0.5, r: 0.5], beta: [a: 3, r: 0.01], gamma: [a: 0.01, r: 4])
).add;
)
f = SimpleMIDIFile.read( "/Users/josephfernandez/Documents/Musee-AF/patch_TDM/midi_seqs/Xylophone_Metallophone/Belle_Cochard_SC.mid")
x = f.p;
f = SimpleMIDIFile.read( "/Users/josephfernandez/Documents/Musee-AF/patch_TDM/midi_seqs/Carmen_Bizet_inD_95_SC.mid")


(
~z=0;
r = Task({
while {
y[~z].delta.notNil;

} {
//notes and delta are extracted -

Synth(\vartest, [freq: y[~z].midinote.value.midicps]);


postln("next "+ ~z);
postln("midinote "+ y[~z].midinote.value);
postln("midiout "+ ~events[~z].noteOn.);
postln("vel "+ ((y[~z].amp.value)*127).round);

y[~z].delta.yield;
~z=~z+1;


}
}).play(quant: TempoClock.default.beats + 1.0);)

m = SimpleMIDIFile.read( "/Users/josephfernandez/Documents/Siena/Max/Max-Ircam-Cours/patch_Max6Jours_old/MaxInteraction-Grégoire/1- Midi/Bach-gavotte.mid" );

MIDIClient.init;
(
~events = m.midiEvents;
~midiout = MIDIOut(1);
~routine = {
var lastTime = 0;
~events.do({ |evt|
(evt[1] - lastTime).wait;
lastTime = evt[1];
switch( evt[2].post,
\noteOn, { ~midiout.noteOn( evt[3..].postln ); },
\noteOff, { ~midiout.noteOff( evt[3..].postln ); },
\cc, { ~midiout.control( evt[3..].postln ); }
);
});
}.fork;
)

(
m.timeMode = \seconds;
~events = m.midiEvents;
// ~midiout = MIDIOut(1);
~routine = {
var lastTime = 0;
~events.do({ |evt|
(evt[1] - lastTime).wait;
lastTime = evt[1];
switch( evt[2].post,
\noteOn, { "noteOn".postln ; evt.postln; },
\noteOff, { "noteOff".postln ; evt.postln;  },
// \cc, { ~midiout.control( evt[3..].postln ); }
);
});
}.fork;
)
~routine.stop

~events[1007]

/// VST instrument
(
SynthDef.new(\vsti, { arg out = 0;
// VST instruments usually don't have inputs
Out.ar(out, VSTPlugin.ar(nil, 2));
}).add;
)*/


