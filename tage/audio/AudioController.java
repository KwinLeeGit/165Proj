package tage.audio;

import tage.*;
import a2.MyGame;
import org.joml.*;

public class AudioController {

    private Engine engine;
	private GameObject avatar;
	private GameObject npc;
    private IAudioManager audioManager;
    private Sound engSound, engSound2;

	public AudioController(Engine engine, GameObject avatar, GameObject npc) {
		this.engine = engine;
		this.avatar = avatar;
		this.npc = npc;
	}



    public void loadSounds() {
		audioManager = engine.getAudioManager();

		AudioResource engineRes = audioManager.createAudioResource("engine.wav", AudioResourceType.AUDIO_SAMPLE);
		AudioResource engineRes2 = audioManager.createAudioResource("engine.wav", AudioResourceType.AUDIO_SAMPLE);


		engSound = new Sound(engineRes, SoundType.SOUND_EFFECT, 50, true);
		engSound2 = new Sound(engineRes2, SoundType.SOUND_EFFECT, 100, true);

		engSound.initialize(audioManager);
		engSound2.initialize(audioManager);

		engSound.setMaxDistance(50f);
		engSound.setMinDistance(0.5f);
		engSound.setRollOff(5f);

		engSound2.setMaxDistance(1000f);
		engSound2.setMinDistance(5f);
		engSound2.setRollOff(1f);
	}

	public void setEarParameters() { 
		Camera camera = (engine.getRenderSystem()).getViewport("MAIN").getCamera();
		audioManager.getEar().setLocation(camera.getLocation());
		audioManager.getEar().setOrientation(camera.getN(), new Vector3f(0.0f, 1.0f, 0.0f));
	}

	public void initSound() {
		engSound.setLocation(avatar.getWorldLocation());
		engSound2.setLocation(npc.getWorldLocation());
		setEarParameters();
		engSound.play();	
		engSound2.play();
	}

	public void updateSound() {

		engSound.setLocation(avatar.getWorldLocation());
		engSound2.setLocation(npc.getWorldLocation());
		setEarParameters();

	}


}