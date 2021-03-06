 Engine_Dronecaster : CroneEngine {
  var <synth;
  // var <in;

  *new { arg context, doneCallback;
    ^super.new(context, doneCallback);
  }

  alloc {
  
    // SynthDef(\InJacks, {
    //   arg out;
    //   var sig_;
    //   sig_ = SoundIn.ar([0,1]);
    //   Out.ar(out, sig_);
    // }).add;
  
    // The TestSine. Old faithful.
    SynthDef(\Sine, {
      arg out, hz=440, amp=0.02, amplag=0.02, hzlag=0.01;
      var amp_, hz_;
      amp_ = Lag.ar(K2A.ar(amp), amplag);
      hz_ = Lag.ar(K2A.ar(hz), hzlag);
      Out.ar(out, (SinOsc.ar(hz_) * amp_).dup);
    }).add;

    // @license
    // Thee rusted satellites gather + sing.
    SynthDef(\Zion, {
      arg out, hz=55.1, amp=0.02, amplag=0.02, hzlag=0.01;
      var amp_ = Lag.ar(K2A.ar(amp), amplag);
      var hz_ = Lag.ar(K2A.ar(hz), hzlag);
      var voiceCount = 5;
      var baseNote = hz_.cpsmidi.round;
      var noteDetune = (baseNote - hz_.cpsmidi).abs;
      var maxAmp = amp_ / voiceCount;
      
      var rand = ({|sampleFreq=1, mul=1, add=0, lag=0.5|
        Latch.ar(WhiteNoise.ar(mul, add), Dust.ar(sampleFreq)).lag(lag)
      });
      
      var voices = (1..voiceCount).collect({ |index|
        Pan2.ar(
          Pulse.ar(
            rand.(0.2, noteDetune, baseNote, 2).midicps * index,
            rand.(0.5, 0.5, 1.5)
          ),
          rand.(0.3),
          rand.(0.1, maxAmp)
        );
      });
      Out.ar(out, Mix.ar(voices));
    }).add;

    // @cfdrake
    SynthDef(\Supersaw, {
      arg out, hz=440, amp=0.02, amplag=0.02, hzlag=0.01;
      var amp_, hz_;
      amp_ = Lag.ar(K2A.ar(amp), amplag);
      hz_ = Lag.ar(K2A.ar(hz), hzlag);
      Out.ar(out, Splay.ar(Array.fill(5, { |i|
        BPF.ar(
          Saw.ar(hz_ * i + SinOsc.kr(0.1 * i, 0, 0.5)),
          100 + (i * 100) + SinOsc.kr(0.05 * i, mul: 100),
          2
        )
      }), 1) * amp_);
    }).add;
    
    // @license
    // Roars through a twisting canyon.
    SynthDef(\Lion, {
      arg out, hz=55.1, amp=0.02, amplag=0.02, hzlag=0.01;
      var amp_ = amp.lag(amplag);
      var hz_ = hz.lag(hzlag);
      var voiceCount = 9;
      var baseNote = hz_.cpsmidi.round;
      var noteDetune = (baseNote - hz_.cpsmidi).abs;
      var maxAmp = amp_ / voiceCount;

      var rand = ({|sampleFreq=1, mul=1, add=0, lag=0.5|
        Latch.kr(WhiteNoise.kr(mul, add), Dust.kr(sampleFreq)).lag(lag)
      });

      var voices = (1..voiceCount).collect({ |index|
        Pan2.ar(
          CombN.ar(
            LFPulse.ar(
              rand.(0.2, noteDetune, baseNote, 2).midicps * index,
              0,
              rand.(0.5, 0.5, 0.5)
            ),
            1,
            rand.(0.3, noteDetune, baseNote, 5).midicps.reciprocal * rand.(0.2, 3, 4).round.lag(5),
            rand.(0.2, 10, 0, 3)
          ).tanh,
          rand.(0.4, 1, 0, 2),
          rand.(0.1, maxAmp)
        )
      });
      Out.ar(out, LeakDC.ar(Mix.ar(voices)));
    }).add;

    context.server.sync;
    
    // synth = Synth.new(\Sine, [\out, context.out_b], context.xg);
    // synth = Synth.new(\Zion, [\out, context.out_b], context.xg);
    // in = Synth.new(\InJacks, [\out, context.out_a], context.xg);
    
    this.addCommand("hz", "f", { arg msg;
      synth.set(\hz, msg[1]);
    });
    
    this.addCommand("amp", "f", { arg msg;
      synth.set(\amp, msg[1]);
    });
    
    this.addCommand("stop", "i", { arg msg;
        synth.free;
    });
    
    this.addCommand("start_sine", "i", { arg msg;
      synth = Synth.new(\Sine, [\out, context.out_b], context.xg);
    });

    this.addCommand("start_zion", "i", { arg msg;
      synth = Synth.new(\Zion, [\out, context.out_b], context.xg);
    });

    this.addCommand("start_lion", "i", { arg msg;
      synth = Synth.new(\Lion, [\out, context.out_b], context.xg);
    });
    
    this.addCommand("start_supersaw", "i", { arg msg;
      synth = Synth.new(\Supersaw, [\out, context.out_b], context.xg);
    });
    
    // this.addCommand("injack", "s", { arg msg;
    //   in = Synth.new(\InJacks, [\out, context.out_b], context.xg);
    // });
   
  }

  free {
    synth.free;
  }
  
}
