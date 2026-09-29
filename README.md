# Somax2Collider

<img width="3342" height="2146" alt="Somax2Collider2" src="https://github.com/user-attachments/assets/f5b67b29-c7ed-4340-a8d3-2ea54d335a55" />

A SuperCollider-based frontend for co-creative improvisation with spatial agents, built on top of [Somax2](https://github.com/DYCI2/Somax2) — an AI-based multi-agent system for human–machine co-improvisation developed at [IRCAM](https://www.ircam.fr).

---

## Overview

Somax2Collider implements the Somax2 client in SuperCollider (SCLang), offering a complementary alternative to the original Max/MSP frontend. It enables:

- **Dynamic multi-agent creation** in real time
- **Spatialized sound** via Higher-Order Ambisonics (HOA) and direct loudspeaker-based diffusion
- **Live coding** alongside a graphical interface
- **Cross-platform** support (macOS, Linux, Windows) through SuperCollider's open-source ecosystem
- **OSC external control** from Max/MSP, Antescofo, or any OSC-capable software

Both the GUI and the SCLang code operate on a shared internal state — actions performed in the interface are immediately reflected in the code environment, and vice versa.

---

## Requirements

### SuperCollider
- [SuperCollider](https://supercollider.github.io) ≥ 3.12

### SuperCollider Libraries (Quarks)
Install via SuperCollider's Quarks system (`Quarks.gui`):

- **[FluCoMa](https://www.flucoma.org)** — Real-time audio descriptor analysis (onset detection, pitch, chroma, MFCC, clustering)
- **[SC-HOA](https://github.com/florian-grond/SC-HOA)** — Higher-Order Ambisonics support
- **[VSTPlugin](https://git.iem.at/pd/vstplugin)** — VST instrument support for MIDI corpus playback
- **[Graphical Module (GM)](https://github.com/SimonDeplat/Graphical-Module)** — GUI widgets (GMStyle, GMFaderSlider, GMCheckButton, etc.)

### Python Server
- **[Somax2 Python server](https://github.com/DYCI2/Somax2)** — The Somax2 backend must be installed and accessible. Somax2Collider communicates with it via OSC.

---

## Installation

1. Clone or download this repository into your SuperCollider Extensions folder:

```
/Users/<username>/Library/Application Support/SuperCollider/Extensions/Somax2Collider
```

Or install directly via SuperCollider Quarks (coming soon).

2. Install the required SuperCollider libraries listed above.

3. Install the Somax2 Python server following its own installation instructions.

4. Recompile the SuperCollider class library: **Language → Recompile Class Library**

---

## Quick Start

```supercollider
// You can create agents and influencers directly from code or from the GUI.

// 1. Create a Somax object (only once)
~somax = Somax.new;

// 2. Open the main GUI window
~somax.gui;

// 3. Launch the Somax2 Python server
// Alternatively, start it from the GUI
~somax.start;

// 4. Start running the server
// Alternatively, run it from the GUI
~somax.run;

// 5. Set the corpus folder path
// Alternatively, select the path from the GUI
~somax.corpus_path(”/path-to-your-corpus-folder”);

// 6. Create agents with a corpus (corpus name without the file extension)
~somax.create_agent(\Agent_1, “MyAudioCorpus”);
~somax.create_agent(\Agent_2, “MyMidiCorpus”);

// 7. Create audio influencers (input channels 0 and 1)
~somax.audio_influencer(\AudioInfluencer_0, \audioIn, 0);
~somax.audio_influencer(\AudioInfluencer_1, \audioIn, 1);

// 8. Connect influencers to agents
// Connections can also be made from the GUI
~somax.connection(\AudioInfluencer_0, \Agent_1);
~somax.connection(\AudioInfluencer_1, \Agent_2);
```

---

## Architecture

Somax2Collider is organized as a class-based framework:

| Class | Role |
|---|---|
| `Somax.sc` | Central control module — OSC communication, agent/influencer management, audio routing |
| `Somax_gui.sc` | Main GUI window — server control, device selection, agent/influencer lists |
| `Player_gui.sc` | Per-agent GUI — corpus, parameters, output mode, presets |
| `Influencer_gui.sc` | Per-influencer GUI — input level, onset/pitch analysis parameters |

---

## OSC External Control

Somax2Collider can be controlled via OSC from any external software (Max/MSP, Antescofo, Pure Data, etc.) on **port 3344**, path `/somax_cmd`.

### Global commands
```
/somax_cmd  create_agent        <AgentName>  <CorpusName>
/somax_cmd  delete_agent        <AgentName>
/somax_cmd  create_influencer   <InfluencerName>  <BusNum>
/somax_cmd  delete_influencer   <InfluencerName>
/somax_cmd  connect             <InfluencerName>  <AgentName>
/somax_cmd  disconnect          <InfluencerName>  <AgentName>
/somax_cmd  run
/somax_cmd  stop
/somax_cmd  load_preset_all     <PresetName>
```

### Agent parameters
```
/somax_cmd  <AgentName>/enabled          <1|0>
/somax_cmd  <AgentName>/continuity       <0.0–10.0>
/somax_cmd  <AgentName>/quality          <0.0–10.0>
/somax_cmd  <AgentName>/probability      <0.0–10.0>
/somax_cmd  <AgentName>/amp              <dB>
/somax_cmd  <AgentName>/time_stretch     <0.5–2.0>
/somax_cmd  <AgentName>/sparse           <1|0>
/somax_cmd  <AgentName>/cut              <1|0>
/somax_cmd  <AgentName>/playing_mode     <1|0>
/somax_cmd  <AgentName>/beat_align       <1|0>
/somax_cmd  <AgentName>/timeout          <seconds, 0=endless>
/somax_cmd  <AgentName>/output_mode      <mono|stereo|multichannel|ambisonic|aoo>
/somax_cmd  <AgentName>/load_corpus      <CorpusName>
/somax_cmd  <AgentName>/load_preset      <PresetName>
/somax_cmd  <AgentName>/add_transform    <semitones>
/somax_cmd  <AgentName>/remove_transform <semitones>
/somax_cmd  <AgentName>/weights          <w1> <w2> <w3> <w4> <w5> <w6>
/somax_cmd  <AgentName>/connect_player   <AgentName2>
/somax_cmd  <AgentName>/win              <1|0>
/somax_cmd  <AgentName>/ambi_distribution <random|front|back|left|right|up|down|center|circle_h|dome|rotating>
```

### Influencer parameters
```
/somax_cmd  <InfluencerName>/amp_in           <dB>
/somax_cmd  <InfluencerName>/amp_out          <dB>
/somax_cmd  <InfluencerName>/onset_threshold  <0.0–2.0>
/somax_cmd  <InfluencerName>/onset_limiter    <ms>
/somax_cmd  <InfluencerName>/pitch_quality    <0.0–1.0>
/somax_cmd  <InfluencerName>/onset_type       <Onset|PitchOnset>
/somax_cmd  <InfluencerName>/enabled          <1|0>
/somax_cmd  <InfluencerName>/win              <1|0>
```

---

## Preset System

Somax2Collider includes a JSON-based preset system for saving and recalling all player parameters. Presets are stored in:

```
~/Library/Application Support/SuperCollider/Somax/presets/
```

Presets are agent-independent — the same preset can be loaded onto any agent. Load via the GUI preset menu or via OSC:

```
/somax_cmd  <AgentName>/load_preset  <PresetName>
/somax_cmd  load_preset_all          <PresetName>
```

---

## Spatialization

Somax2Collider supports multiple output modes per agent:

- **Mono / Stereo / Multichannel** — standard SuperCollider audio routing
- **Ambisonics (HOA)** — Higher-Order Ambisonics via SC-HOA, with configurable spatial distributions:
  `random`, `front`, `back`, `left`, `right`, `up`, `down`, `center`, `front_left`, `front_right`, `back_left`, `back_right`, `circle_h`, `dome`, `rotating`
- **AOO** — Audio over OSC

The ambisonics distribution can be set per agent from the GUI or via OSC, enabling dynamic and content-aware spatialization.

---

## Corpus Builder

Build and analyze new corpora directly from SuperCollider:

```supercollider
~somax.corpus_builder("MyFile.aif", peak_thresh: 0.1);
```

The Corpus Builder uses FluCoMa to generate onset detection, MFCC, pitch, chroma, and 2D clustering analyses, stored alongside the corpus for use in spatialization and navigation.

---

## Repository

- **Somax2Collider**: [https://github.com/DYCI2/Somax2Collider](https://github.com/DYCI2/repositories)
- **Somax2 Python server**: [https://github.com/DYCI2/Somax2](https://github.com/DYCI2/Somax2)
- **DYCI2 organization**: [https://github.com/DYCI2](https://github.com/DYCI2)

---


## Acknowledgments

Somax2Collider is developed at IRCAM as part of the DYCI2 / REACH project (Raising Cocreativity in Cyber–Human Musicianship), funded by the European Research Council.

Thanks to the SuperCollider community and to the developers of FluCoMa, SC-HOA, VSTPlugin, and Graphical Module for their essential contributions.

---

## License

This project is distributed under the **Creative Commons Attribution License 3.0 Unported**. See [LICENSE](LICENSE) for details.
