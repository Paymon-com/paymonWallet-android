package ru.paymon.android;

public class Config {
    public static final boolean DEBUG = false;
    public static final boolean PRODUCTION_VERSION = true;
    public static final boolean TEST_BITCOIN = false;

    public static String HOST;
    public static String HOST_ALT;

    public static final String TAG = "paymon-dbg";

    public static short PORT = 7966;
    static {
        if (DEBUG) {
            HOST = "192.168.88.168";
            HOST_ALT = "192.168.88.168";
            PORT = 7968;
        } else {
            HOST = "91.226.80.26";
            HOST_ALT = "91.226.80.26";
        }
    }

    public static final int VERSION = 0x1_08b;
    public static final String VERSION_STRING = "0.8 beta";
    public static final int READ_BUFFER_SIZE = 1024 * 128;
    public static final byte MAX_SEND_ATTEMPTS = 3;
    public static final int KEY_GUARD_TIMEOUT = 60;

    public static final String FIRST_TIME_OPEN_MONEY = "first_time_open_money";

    public static final String USER_INFO = "user_info";
    public static final String APP_PREFERENCE_NOTIF = "settings_notification_";
    public static final String APP_PREFERENCE_NOTIF_WORRY = "switch_notif_worry";
    public static final String APP_PREFERENCE_NOTIF_SOUND = "notif_sound";
    public static final String APP_PREFERENCE_NOTIF_VIBRATION = "switch_notif_vibration";
    public static final String APP_PREFERENCE_NOTIF_POP_UP = "switch_notif_pop_up";
    public static final String APP_PREFERENCE_NOTIF_TRANSACTION = "switch_notif_transactions";
    public static final String APP_PREFERENCE_NOTIF_INVITE_FRIEND = "switch_notif_invite_to_friend";
    public static final String APP_PREFERENCE_NOTIF_INVITE_GROUP = "switch_notif_invite_to_group";
    public static final String APP_PREFERENCE_NOTIF_GAME = "switch_notif_game";

    public static final String APP_PREFERENCE_BASIC = "settings_basic_";
    public static final String KEY_FRIENDS = "basic_sort_friends_list";
    public static final String KEY_BACK = "basic_chat_background_list";

    public static final String APP_PREFERENCE_SECURITY = "settings_security_";
    public static final String APP_PREFERENCE_CHECK_PROTECT = "check_password_protect";
    public static final String APP_PREFERENCE_PASSWORD_PROTECT = "edit_password_protected";

    public static final String APP_PREFERENCE_MONEY_BTC = "settings_money_btc_";
    public static final String APP_PREFERENCE_MONEY_ETH = "settings_money_eth_";
    public static final String APP_PREFERENCE_MONEY_DENOMINATION_BTC = "money_denomination_list_btc";
    public static final String APP_PREFERENCE_MONEY_DENOMINATION_ETH = "money_denomination_list_eth";

    public static final String YANDEX_WALLET_PREFERENCE = "yandex_wallet_";
    public static final String ETHEREUM_WALLET_PREFERENCE = "ethereum_wallet_";
    public static final String PAYEER_WALLET_PREFERENCE = "payeer_wallet_";
    public static final String PAYEER_WALLET_TOKEN = "payeer_token";
    public static final String ETHEREUM_WALLET_PASSWORD = "ethereum_password";
    public static final String ETHEREUM_WALLET_BACKUP = "ethereum_beckup";
    public static final String HAVE_WALLET_PREFERENCE = "have_wallet";
    public static final String PMNT_PREFERENCE = "pmnt_preference_";
    public static final String HAVE_PMNT = "have_pmnt";
    public static final String PMNT_ADDRESS = "pmnt_address";

    public static final String KEY_CONFIRMED_EMAIL = "confirmed_email";

    public static final String KEY_CHAT_TRANSFER = "chat_transfer";
    public static final String KEY_CHAT_ID = "chat_id";

    // sets automatically
    public static boolean isTablet;

    // keys for Bundle
    public static final String KEY_DEPOSIT = "deposit";
    public static final String KEY_WITHDRAW = "withdraw";

    public static final String KEY_CURRENCY_ID = "currency_id";
    public static final String KEY_FIAT_ID = "fiat_id";
    public static final String KEY_ACTION_ID = "action_id";

    public static final String KEY_ACCOUNT_ID = "account_id";
    public static final String KEY_PAYMENT_ID = "payment_id";

    public static final String KEY_AMOUNT = "amount";
    public static final String KEY_COMMISSION = "commission";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_BALANCE = "balance";

    public static final String KEY_GAS_PRICE = "gas_price";
    public static final String KEY_GAS_LIMIT = "gas_limit";

    public static final String KEY_TRANSFER = "transfer";


    public static final String PAYEER = "payeer";
    public static final String YANDEX = "yandex";

    public static final String KEY_OPEN_FRIEND_PROFILE_FROM_CHAT = "open_profile_from_chat";
    public static final String KEY_USER_ID = "user_id";

    public static final String KEY_CONFIRMATION_CODE = "confirmation_code";

    // keys currency
    public static final String RUB = "\u20BD";
    public static final String USD = "\u0024";
    public static final String EUR = "€";
    public static final String BTC = "BTC";
    public static final String ETH = "ETH";
    public static final String GWEI = "GWEI";

    //FragmentListWallet transaction params
//    public static final int ADD = 0;
//    public static final int WITHDRAW = 1;


    //qr code scan
    public static final String ETHEREUM_WALLET = "ethereum:0x";
    public static final String ETHEREUM_WALLET_2 = " 0x";
    public static final String ETHEREUM_WALLET_3 = "0x";

    public static final String BITCOIN_WALLET = "bitcoin:1";
    public static final String BITCOIN_WALLET_2 = "BITCOIN:-";
    public static final String BITCOIN_WALLET_3 = "1";
    public static final String BITCOIN_WALLET_4 = "3";
    public static final String BITCOIN_WALLET_5 = "bitcoin:3";

    //min amount for transaction
    public static final Double MIN_BTC = 0.0005;
    public static final Double MIN_GAZ_PRICE = 4.0;

    public static final Double gweiToEth = 1000000000.0;
    public static final long TEN_8 = 100000000L;
    public static final long TEN_8_DOUBLE = 100000000;


    public static final Integer MAX_DEPOSIT = 1000000;

    public static int CONFIRMATION_CODE = 0;
    public static String CONFIRMATION_LOGIN = "";

    //wallets
    public static String BTC_CRYPTO_BALANCE = "";
    public static String BTC_FIAT_BALANCE = "";

    public static String ETH_CRYPTO_BALANCE = "";
    public static String ETH_FIAT_BALANCE = "";
    public static String ETH_PUBLIC_ADDRESS = "";
    public static String ETH_PRIVATE_ADDRESS = "";

    public static String PMNT_CRYPTO_BALANCE = "";
    public static String PMNT_FIAT_BALANCE = "";

    public static int REQUEST_CODE_SCAN = 0;
}
