Plotter_clusters_gui {

	var <>win, <>plotter;

	*new {|player, audio_buffer, index_buffer, outbus, ds_redux, labels, server, parentClass|
		^super.newCopyArgs(player, audio_buffer, index_buffer, outbus, ds_redux, labels, server, parentClass).init_plotter(player, audio_buffer, index_buffer, outbus, ds_redux, labels, server, parentClass);
	}

	init_plotter {|player, audio_buffer, index_buffer, outbus, ds_redux, labels, server, parentClass|

		// arg ds, action;
		var kdtree = FluidKDTree(server);
		var buf_2d = Buffer.alloc(server,2);
		var scaler = FluidNormalize(server);
		var ds_norm = FluidDataSet(server);

		FluidNormalize(server).fitTransform(ds_redux, ds_norm);

		// labels = FluidLabelSet(server);

		// FluidKMeans(server,4).fitPredict(ds_redux, labels); // try with a different number of clusters
		/*	var ds_norm = FluidDataSet(s);*/
		win = Window(player.asString,Rect(50,50,300,300));
		win.view.deleteOnClose = false; // win not destroyed
		// whatever the output of umap is, scale it to be between 0 and 1 so that it will look nice in the plotter
		// FluidNormalize(s).fitTransform(ds,ds_norm);

		kdtree.fit(ds_norm);

		ds_norm.dump({
			arg dict;
			var previous, fp, cluster_lab;
			fork({
				plotter = FluidPlotter(win, bounds:Rect(0,0,300,300),dict:dict,mouseMoveAction:{
					arg view, x, y;
					buf_2d.setn(0,[x,y]); // load it into a buffer so that...
					kdtree.kNearest(buf_2d,1,{ // it can be passed to the kdtree to find hte nearest neighbour, which is reported back...
						arg nearest; // here
						nearest.postln;
						if(previous != nearest,{ // only if it is a "new" nearest neighbour, should it make sound
							var index = nearest.asString.split($-)[1].asInteger; // peel off the index of the slice
							previous = nearest;
							// index.postln;
							// "nearest".postln;
							// nearest.postln;
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
							Synth(\Play_slice_cluster, [\buf, audio_buffer, \offset_ch, outbus, \idx, index_buffer, \index, index, \cluster, cluster_lab.asInteger]);
							"cluster_lab "++cluster_lab.postln;
						});
					});
				});
				plotter.pointSizeScale_(0.5);
				labels.dump{
					arg labelsdict;
					labelsdict.postln;
					plotter.categories_(labelsdict);
				}
			}, AppClock);
		});
	}
}

/*(
// Play slices
SynthDef(\Play_slice_cluster, { |outbus = -1, offset_ch = 0, buf, idx, startsamp, stopsamp, rate = 1,
	amp = 0, fade = 0.03, free = 1, loop = 0, t_trig = 0, seg = 0, index, cluster = 0|

	var buf_frames = BufFrames.kr(buf);
	var sr = BufSampleRate.ir(buf);
	// var start = (startsamp*0.001)*sr;
	var start = Index.kr(idx, index); // look up the start position
	var dur_samps = Index.kr(idx, index + 1) - start; // calculate the duration in samples
	var dur_sec = min(dur_samps / BufSampleRate.ir(buf),1);
	// var end = (stopsamp*0.001)*sr;
	var sig = PlayBuf.ar(1, buf, BufRateScale.kr(buf) * rate, t_trig, start, loop: loop, doneAction:2);
	var dursecs = BufDur.kr(buf); //(end - start ) ;
	var envgate, sig_mix, done;
	// Line.ar(start: 0.0, end: 1.0, dur: env_dur-del, doneAction: 2); // free grain after delay

	// envgate = EnvGen.kr(Env.asr(fade, 1.0, fade, \welch ), free, doneAction:2);
	var env = EnvGen.kr(Env([0,1,1,0],[0.03,dur_sec-0.06,0.03]),doneAction:2);
	var choose_cluster, sel_hp, speakers, cluster_amp, sel_amp;

	// speakers = \speakers.kr([7, 13, 17]);
	// speakers = \speakers.kr([[0, 1], [6, 9], [10, 13]]); // 18 speakers
	speakers = \speakers.kr([[0, 5], [6, 11], [12, 17], [18, 23]]); // 24 speakers
	// speakers = \speakers.kr([[6, 13]]);
	cluster_amp = \cluster_amp.kr([0, 6, 6]);

	sel_hp = Select.kr(cluster, speakers);
	sel_amp = Select.kr(cluster, cluster_amp);

	choose_cluster = TIRand.kr(sel_hp[0], sel_hp[1], trig: t_trig);
	// choose_cluster = TChoose.kr(t_trig, sel_lo);

	// choose_cluster.poll;
	sig = sig * env *sel_amp.dbamp;
	done = Done.kr(env);
	SendTrig.kr(done, 0, seg);
	sig_mix = Mix.ar(sig);
	Out.ar(offset_ch+choose_cluster, sig * amp.dbamp);
	// ReplaceOut.ar(outbus, sig_mix*amp.dbamp)
}).load;
)*/