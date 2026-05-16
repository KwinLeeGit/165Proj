package tage.audio;

import tage.*;
import a2.MyGame;
import org.joml.*;
import java.util.*;
import java.math.*;
import tage.networking.client.*;

public class AudioController {

    private Engine engine;
	private GameObject avatar;
	private ArrayList<GameObject> npcs;
    private IAudioManager audioManager;
    private Sound engSound;
	private ArrayList<Sound> npcEngineSounds = new ArrayList<>();
	private GhostManager ghostManager;
	private ArrayList<Sound> ghostEngineSounds = new ArrayList<>();

	public AudioController(Engine engine, GameObject avatar, ArrayList<GameObject> npcs, GhostManager ghostManager) {
		this.engine = engine;
		this.avatar = avatar;
		this.npcs = npcs;
		this.ghostManager = ghostManager;
	}



    public void loadSounds() {
		audioManager = engine.getAudioManager();

		AudioResource engineRes = audioManager.createAudioResource("engine.wav", AudioResourceType.AUDIO_SAMPLE);
		
		
		for (int i = 0; i < npcs.size(); i++) {

            AudioResource engineRes2 =audioManager.createAudioResource("engine.wav",AudioResourceType.AUDIO_SAMPLE);

            Sound engineSound = new Sound(
                engineRes2,
                SoundType.SOUND_EFFECT,
                100,
                true
            );

			

            engineSound.initialize(audioManager);

			engineSound.setPitch(0.9f + (float)java.lang.Math.random() * 0.4f);

            engineSound.setMaxDistance(1000f);
            engineSound.setMinDistance(5f);
            engineSound.setRollOff(.5f);

            npcEngineSounds.add(engineSound);
        }


		engSound = new Sound(engineRes, SoundType.SOUND_EFFECT, 80, true);

		engSound.initialize(audioManager);

		engSound.setMaxDistance(100f);
		engSound.setMinDistance(5f);
		engSound.setRollOff(1f);

	}

	public void setEarParameters() { 
		Camera camera = (engine.getRenderSystem()).getViewport("MAIN").getCamera();
		audioManager.getEar().setLocation(camera.getLocation());
		audioManager.getEar().setOrientation(camera.getN(), new Vector3f(0.0f, 1.0f, 0.0f));
	}

	public void initSound() {
		engSound.setLocation(avatar.getWorldLocation());

		for (int i = 0; i < npcs.size(); i++) {

            Sound sound = npcEngineSounds.get(i);
            GameObject npc = npcs.get(i);

            sound.setLocation(npc.getWorldLocation());
            sound.play();
        }
		setEarParameters();
		engSound.play();	
	
	}

	public void updateSound() {

		engSound.setLocation(avatar.getWorldLocation());

		for (int i = 0; i < npcs.size(); i++) {

            GameObject npc = npcs.get(i);
            Sound sound = npcEngineSounds.get(i);

            sound.setLocation(npc.getWorldLocation());
        }

		updateGhostSounds();

		setEarParameters();

	}

		private Sound createEngineSound(int volume) {
		AudioResource res = audioManager.createAudioResource(
			"engine.wav",
			AudioResourceType.AUDIO_SAMPLE
		);

		Sound sound = new Sound(res, SoundType.SOUND_EFFECT, volume, true);
		sound.initialize(audioManager);

		sound.setPitch(0.9f + (float)java.lang.Math.random() * 0.4f);

		sound.setMaxDistance(1000f);
		sound.setMinDistance(5f);
		sound.setRollOff(.5f);

		return sound;
	}

	private void updateGhostSounds() {
		if (ghostManager == null) return;

		Vector<GhostAvatar> ghosts = ghostManager.getGhostAvatars();

		while (ghostEngineSounds.size() < ghosts.size()) {
			Sound sound = createEngineSound(100);
			sound.play();
			ghostEngineSounds.add(sound);
		}

		for (int i = 0; i < ghosts.size(); i++) {
			GhostAvatar ghost = ghosts.get(i);
			Sound sound = ghostEngineSounds.get(i);

			sound.setLocation(ghost.getWorldLocation());
		}
	}


}