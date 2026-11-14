---
id: "2026-11.tq7q"
slug: a-mesh-call-can-keep-what-its-speaker-says-as-voice-messages
title: "A mesh call can keep what its speaker says as voice messages"
date: 2026-11-14
topics: [calls, voice]
---

# ADR 2026-11.tq7q — A mesh call can keep what its speaker says as voice messages

Status: Accepted (2026-11-14)

**What was observed.** A mesh call's bursts stream as sealed `talk` frames nobody custodies (79yb's 2026-10-09
amendment), so nothing a person says in one survives the call. People asked to keep a record of what they said, the
way a voice message stays in the thread.

**What changed.** The Calls screen gains "Save what I say in mesh calls" (`CallPreferenceStore.keepBursts`, key
`calls_keep_bursts`, off by default). On, `BurstNoteKeeper` copies each burst this phone speaks off `TalkRecorder`'s
second PCM tap (`keep`), encodes it to AAC-LC in ADTS at 24 kbit/s (`ui/call/BurstAac`, headers from
`VoiceAudio.adtsHeader`) once the burst ends, and holds it. When the session ends (`WalkieTalk.session`'s `finally`),
each held note is ingested with `AttachmentStore.ingestVoice` and sent to the peer with `MeshController.sendChat`, so it
is an ordinary sealed DM voice message: custodied, pulled as a blob, played, measured and transcribed by every build.

- **Only the speaker's own voice, only by the speaker's switch.** A burst heard is never copied, and nothing a peer
  sends starts, names or fills a note.
- **After the call, not during it.** A note's blob would otherwise compete with the call's chunks for the same slow
  link. Past 4 MiB held (about twenty minutes of talk) the oldest goes out early.
- **The slipped-thumb rule holds.** A burst the session calls too short, a cancel, and a microphone that would not
  open keep nothing; neither does a burst under 500 ms of audio.

Rejected:

- **Reassembling the Opus chunks into a note.** No build plays an Opus container in the thread, and the iOS port
  compares the MIME exactly; a new MIME would be a wire question. Re-encoding the PCM keeps `audio/aac`.
- **Saving both sides.** Recording the peer is a consent question this switch cannot answer; the peer keeps their own.

**What it costs.**

- A process that dies mid-call loses the notes it held: they live in memory until the session ends.
- The peer sees the bursts twice, live and later as voice messages; that is the point of the switch, and why it is off
  by default.
- There is no `mesh/lab/` scenario: the lab cannot run calls (3tee's amendment), and a voice note is a message the
  lab already converges. A device trial owes: two phones, a mesh call with the switch on, three bursts, hang up, and
  three voice messages in the peer's thread in order.
