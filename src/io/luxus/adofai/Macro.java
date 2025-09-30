package io.luxus.adofai;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.json.simple.parser.ParseException;

import io.luxus.api.adofai.ADOFAIMap;
import io.luxus.api.adofai.MapData;
import io.luxus.api.adofai.Tile;
import io.luxus.api.adofai.action.Action;
import io.luxus.api.adofai.type.EventType;

public class Macro {
	private static class DelayEntry {
		final long nanos;
		final boolean press;
		DelayEntry(long nanos, boolean press) {
			this.nanos = nanos;
			this.press = press;
		}
	}
	
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		try {
			Macro.program(scanner);
			System.out.println("Press ENTER to Continue.");
			System.in.read();
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			scanner.close();
		}
	}
	
	
	private static void program(Scanner scanner) throws IOException, ParseException, AWTException {
		System.out.println("A Dance of Fire and Ice Macro");
		System.out.println("ver 1.0.0");
		System.out.println("Developer : Luxus.io");
		System.out.println("YouTube : https://www.youtube.com/channel/UCkznd9aLn0GXIP5VjDKo_nQ");
		System.out.println("Github : https://github.com/Luxusio");
		System.out.println();
		
		System.out.print("Map File Path : ");
		
		String path = scanner.nextLine().trim();
		
		MapData mapData = new MapData();
		mapData.load(path);
		
		ADOFAIMap adofaiMap = new ADOFAIMap(mapData);
		
		System.out.print("Delay : ");
		int loadDelay = scanner.nextInt();
		scanner.nextLine();

		List<DelayEntry> delayList = new ArrayList<>();
		BigDecimal result = bd("60000").divide(bd(mapData.getSetting().getBpm()), 100, RoundingMode.HALF_EVEN).multiply(bd("6.5")).add(bd(mapData.getSetting().getOffset())).add(bd(loadDelay)).multiply(bd("1000000"));
		long longResult = (long) result.longValue();
		delayList.add(new DelayEntry(longResult, false));
		BigDecimal error = result.subtract(bd(longResult));
		
		List<Tile> tileList = adofaiMap.getTileList();
		int size = tileList.size();
		
		for(int i=1;i<size;i++) {
			Tile tile = tileList.get(i);
			if(tile.getRelativeAngle() != 0) {
				double tempBPM = tile.getTempBPM();
				
				result = bd("60000.0").divide(bd(tempBPM), 100, RoundingMode.HALF_EVEN).multiply(bd("1000000"));
				
				
				longResult = (long) result.longValue();
				error.add(result.subtract(bd(longResult)));
				if(error.compareTo(bd("1000000.0")) == 1) {
					long longError = error.longValue();
					longResult -= longError;
					if(longResult < 1000000) {
						System.out.println("E: delay is smaller than 1ms");
						longResult = 1000000 - longResult;
						longError += longResult;
					}
					error = error.subtract(bd(longError));
				}
				
				delayList.add(new DelayEntry(longResult, true)); // ★従来の叩き: 押す
				                // ★追加: Pause イベント対応
                List<Action> pauses = tile.getActionListIfNotEmpty(EventType.PAUSE);
                if (pauses != null) {
                    for (Action a : pauses) {
                        // durationは拍数。1拍 = 60000 / bpm [ms]
                        double beats = ((io.luxus.api.adofai.action.Pause)a).getDuration();
                        if (beats > 0) {
                            BigDecimal pauseMs = bd("60000").divide(bd(tempBPM), 100, RoundingMode.HALF_EVEN) // 1拍(ms)
                                    .multiply(bd(beats));
                            long pauseNs = pauseMs.multiply(bd("1000000")).longValue();
                            if (pauseNs < 1000000) pauseNs = 1000000; // 1ms未満は切り上げ（任意）
                            delayList.add(new DelayEntry(pauseNs, false)); // ★押さない待機
                        }
                    }
                }
			}
		}
		
		Robot robot = new Robot();
		
		System.out.println();
		System.out.println("Place your cursor on the play button. Start.");
		
		
		for(int i=5;i>0;i--) {
			System.out.println(i);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("start");
		
		robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
		
		Iterator<DelayEntry> it = delayList.iterator();
		
		long prev_nanos = System.nanoTime();
		long curr_nanos;
		
		boolean mask = false;
		
		DelayEntry de = it.next();
		long delay = de.nanos;
		boolean pressAfterDelay = de.press;
		boolean keep = true;
		boolean nextKeep = it.hasNext();
		
		
		while (keep) {
			curr_nanos = System.nanoTime();
			if(curr_nanos - prev_nanos >= delay) {
				prev_nanos += delay;
				//System.out.println("c:" + delay / 1000000.0 + "ms");
				mask = !mask;
				if(mask) {
					robot.keyPress('E');
					robot.keyRelease('E');
				} else {
					robot.keyPress('P');
					robot.keyRelease('P');
				}
				if(nextKeep) {
					de = it.next();
					delay = de.nanos;
					pressAfterDelay = de.press;
				}
				keep = nextKeep;
				nextKeep = it.hasNext();
			}
			
//			try {
//				Thread.sleep(1L);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
		}
		
		System.out.println("end");
		
		
	}
	
	public static BigDecimal bd(long value) {
		return bd(String.valueOf(value));
	}
	
	public static BigDecimal bd(double value) {
		return bd(String.valueOf(value));
	}
	
	public static BigDecimal bd(String str) {
		return new BigDecimal(str);
	}
	
}