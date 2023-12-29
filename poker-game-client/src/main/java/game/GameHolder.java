package game;

import game.rule.CommonRule;
import game.rule.GameRule;

public class GameHolder {

   public static GameRule rule=new CommonRule();
   /**
    * 加载器加载的Game实例引用<p>
    * 同时避免从Room多层取该实例。
    */
   public static Game game;

}
