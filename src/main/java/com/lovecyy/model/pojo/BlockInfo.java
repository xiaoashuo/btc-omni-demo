package com.lovecyy.model.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class BlockInfo {

    /**
     * tx : ["097e793f07028cab84677b6b8c730e9aa3ab4409818f18b5040865cb1f643c2c","1ded4eefea19d560b6f47ec0ca3fe2e2a774fc72fe2c34572756e5e59fb9f3a0","c65a005568980c9fc557ed40cbe5e884877a27333fd759b6cc17527be89a6138","09c3ddb23ebb1c376862f20250b6287dd8db516a52ec409d43b7980fda66fe8a","4b1014209d639d48b8f5635e9cc39cce9bee6010bfd65a289fe25337df4be27d","a72bd825b380eb0cf8768d5351eb76f7a150845b4c015360d0e55cfc0bcdae31","9da5fe6a25807af4b446d1ba4ab9ec0327bac5cf971a0489c4f26c54f2f67c0d","8589852a14dba9764e40c25ecfe7ae0208e504fd35927efc2bf82e02b5f4558f","3157aafc22927388f780f4db3fd4ba5e090cc01d7a6e3710a592b82f5d10060d","a4c19585a82aa0167125fa891ae91a6babae769109937b80214ddfe51a947bc3","59f4d58e4769f25519a5282e5b36fcab4757a38dc7e91275045bed911bb03742","43f5d16008880657f176d67e1bb4d5f8c377df6d8c8d45b9974e096a4fab439d","80efa534b92c636b258f5f6b0ad371a55b7bf98422b3415ff6d8e9147965eadd","88f9d40c71f9e8dd092c5808cdd324f704e4239dfb11060d9f7eaf600f0775a7","0ecc648729ae17e644e0f65636a5bb21d329949179a6a66232f2f6231eb30613","a12f97ac0235a2f8ce305df652abce1becbb4e53510ba8378f8e4ea55e6cf174","c316a2c4514203c220cc08c4c9fe8a03bfacbfbf22f7c04fff6ea3717884883f","b1e18375443fb85cf9a022ae56eb0bbfe4cf5ddb71b06b15e979c2a077a16d04","c1821668ae9afb9d7a48104ad447de9ee7731af3fc4cf0b7c578cf94a5d9a967","63d7e22de0cf4c0b7fd60b4b2c9f4b4b781f7fdb8be4bcaed870a8b407b90cf1","0a748a787b5a12df312f24ed376ce4c00306906e68138372f6241b0f3c5bc123","92c779495a8b177392b32f3d46baa25348d1229904337e6a7e24fbcd85c7a9dc","6fb25ab84189d136b95d7f733b0659fa5fbd63f476fb1bca340fb4f93de6c912","d54213046d8be80c44258230dd3689da11fdcda5b167f7d10c4f169bd23d1c01","55d3125eee26c9ddc5005ef8aac86f99baecc4979c67959841bfe9ab45ec83f6","877fa05215bfe255e0bdeca89a6429203d8b9b15bf0edb28edb432e460f1b570","e2c252423af500e53f4855cf2a2ccdd6580d7dd42953d23d8bfac787b3bb50a4","51ff8dc2104d058eed0de9bdcc4032eb2a044e044281c8d8a1e0f510ff5e76f7","eb4e3405288ab9e9e38f1e8f31c38e24a79cccdbde6b83bfe45cc9921fc138f9","5874f6b1d0f36d9fc7f73f4117fa2a20b0005468bb02b48bc1c721da7197d1eb","5c99fa7151e9767d0eda3aae332cddee5b8f9fa3f41eddb13c2b3815be777c23","c3c61a2a24ee480d72a08fbbb998490f0b0ed849750185ffd8f3a7f04f01dc1e","4837a3f71965a0ea7f7292e14f55ed65183ebfd236d06453187cd79ed35f25ab","63dcea2bd468e00c8fc6d1265800482951f0ac7c6620bde681529121ae661661","3bbf37089eea372299eab005eaeca9122d2be7f59f9356a6d094dccf56c493ab","cad68d3469c8c601709c08c0fa57b180ecdd4379cb11590d6969cbf086c03852","ca04c42f73b7b38c1155cfa9bb8d1eefe217be47302132bc4127a25cc3bf309b","8e558d2926f6d0c13cd0d2fdf0599ef3d86beec78759dd69a81c5131bddd7b34","38e7fb1cfd471a7e45ad67cbd0d6983f176dfb6f105ba35d8d8cf03605deb712","95dadf647615fba21f00390c7141ea544940a5c3c0886f11867d60ce6468e57d"]
     * mediantime : 1409938695
     * previousblockhash : 000000006893ef1c3b48fb2a5b41940c0afe85bccdd1e8d01f4aabda04b183d9
     * bits : 1d00ffff
     * weight : 40532
     * versionHex : 00000002
     * confirmations : 1623783
     * version : 2
     * nonce : 624308544
     * nextblockhash : 00000000466e5c07e3a2d96c2c62465239d6ea352db1ced6fb5a82bd4cc7e7ee
     * difficulty : 1
     * chainwork : 000000000000000000000000000000000000000000000000492203c3fb39f595
     * nTx : 40
     * size : 10133
     * merkleroot : f483fdee82b92ff4901eafbafe7157dd90a84b2f9bb6543a3a405cd13cf14b9a
     * strippedsize : 10133
     * time : 1409941113
     * hash : 000000002101ea0da161b6a63f4d1a0e37a2bd58c5aee49a3b8fd80640b09662
     * height : 279007
     */
    /**
     * 区块hash
     */
    private String hash;
    /**
     * 前一个块hash
     */
    private String previousblockhash;
    /**
     * 下一个块hash
     */
    private String nextblockhash;
    /**
     * 区块中值时间戳
     */
    private Integer mediantime;
    /**
     * 存储难度的16进制目标值
     */
    private String bits;
    /**
     * BIP141定义的区块权重
     */
    private Integer weight;
    /**
     * 版本
     */
    private Integer version;
    /**
     * 16进制表示的版本
     */
    private String versionHex;
    /**
     * 确认数
     */
    private Integer confirmations;
    /**
     * nonce值
     */
    private Long nonce;
    /**
     * 难度
     */
    private Integer difficulty;
    /**
     *
     */
    private String chainwork;

    /**
     * 区块字节数
     */
    private Integer size;
    /**
     * 区块的莫克尔树根
     */
    private String merkleroot;
    /**
     * 剔除隔离见证数据后的区块字节数
     */
    private Integer strippedsize;
    /**
     * 区块创建时间戳
     */
    private Integer time;
    /**
     * 区块高度
     */
    private Integer height;
    /**
     * 区块包含交易大小
     */
    @JsonProperty("nTx")
    private Integer nTx;
    /**
     * 区块中所有交易组成的数组，成员为交易id
     */
    private List<String> tx;

}
