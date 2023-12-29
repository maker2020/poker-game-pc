package game.poker.common;

/**
 * 牌值枚举：<p>
 * <code>value</code>:str<p>
 * <code>code</code>:整型标识。通常配合索引使用（如：发牌）<p>
 * <code>weight</code>:权重。这个值很重要，在比对逻辑时（如：顺子不能123等不合规）发挥作用。
 */
public enum PokerValueEnum {
    A("A",1,12),
    Two("2",2,13),
    Three("3",3,1),
    Four("4",4,2),
    Five("5",5,3),
    Six("6",6,4),
    Seven("7",7,5),
    Eight("8",8,6),
    Nine("9",9,7),
    Ten("10",10,8),
    J("J",11,9),
    Q("Q",12,10),
    K("K",13,11),
    Queen("X",14,100),
    King("Y",15,101);

    private String value;
    private int code;
    private int weight;

    PokerValueEnum(String value,int code,int weight){
        this.value=value;
        this.code=code;
        this.weight=weight;
    }

    public static PokerValueEnum getByValue(String value){
        PokerValueEnum[] enums=values();
        for(PokerValueEnum res:enums){
            if(res.value.equals(value)) return res;
        }
        return null;
    }

    public static PokerValueEnum getByCode(int code){
        PokerValueEnum[] enums=values();
        for(PokerValueEnum res:enums){
            if(res.code==code) return res;
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public String toString(){
        return value;
    }
}
