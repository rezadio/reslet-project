package helper;

import com.google.gson.JsonObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 *
 */
public class Constant {

    public static final String VERSION = "v1.4.5";

    public static String KUNCI = "4ss4l4mu4l41kumw4r0hm4tull4h!.wb";

    public static String key = "7d9b24c3-ca9f-43a9-9aa4-be82b9c496d7";
    public static final String RSV = "Layanan Bantuan Hubungi Truemoney (0804) 1000 100";
    public static int idSupplierBimaSakti = 15;
    public static int idSupplierVsi = 14;

    public static String KARTU_MEMBER = "KARTU_MEMBER";
    public static String AGENT = "AGENT";
    public static String MEMBER = "MEMBER";

    public static int idPromo = 2;
    public static String userId = "truemoneydev";
    public static String pin = "IIV9HHSQEOOQ106A";

    public static final int TIPETRANSAKSI_CASHIN = 2;
    public static final int TIPETRANSAKSI_CASHOUT = 3;
    public static final int TIPETRANSAKSI_PAYMENT = 59;
    public static final int TIPETRANSAKSI_PURCHASE = 76;
    public static final int TIPETRANSAKSI_PAYMENT_RETAIL = 20;
    public static final int TIPETRANSAKSI_PULSA = 17;
    public static final int TIPEAPLIKASI = 4;

    public static final String MAX_COUNT_DAY_KYC = "MAX_COUNT_DAY_KYC";
    public static final String MAX_AMOUNT_DAY_KYC = "MAX_AMOUNT_DAY_KYC";
    public static final String MAX_COUNT_DAY_NONKYC = "MAX_COUNT_DAY_NONKYC";
    public static final String MAX_AMOUNT_DAY_NONKYC = "MAX_AMOUNT_DAY_NONKYC";
    public static final String MAX_AMOUNT_DAY_AGENT = "MAX_AMOUNT_DAY_AGENT";

    public static final String VERIFIED = "VERIFIED";
    public static final String VERSIBASEAPI = "BASE API TRUEMONEY 2.1.1 ";

    public static final int ID_OPERATOR_CASHIN = 221;
    public static final int ID_OPERATOR_CASHOUT = 222;

    public static final String TRX_CASHIN = "CREDIT";
    public static final String TRX_CASHOUT = "DEBIT";
    public static final String TRX_PAYMENT = "PAY";
    public static final String TRX_PURCHASE = "PURCHASE";

    public enum StatusTrx {
        SUKSES("SUKSES"),
        GAGAL("GAGAL"),
        PENDING("PENDING"),
        SUSPECT("SUSPECT"),
        OPEN("OPEN");

        private String status;

        StatusTrx(String vStatus) {
            this.status = vStatus;
        }

    }

    public class tx_type {

        public static final String purchaseValidationReq = "012";
        public static final String purchaseTransactionReq = "112";
        public static final String purchaseReversalReq = "912";

        public static final String cashInValidationReq = "011";
        public static final String cashInTransactionReq = "111";
        public static final String cashInCommitReq = "811";

        public static final String cashOutValidationReq = "013";
        public static final String cashOutTransactionReq = "114";
        public static final String cashOutReversalReq = "914";
    }

    public enum ACK {
        OK("OK"), NOK("NOK"), SUSPECT("SUSPECT");

        private String ack;

        ACK(String ack) {
            this.ack = ack;
        }

    }

    public enum StatusAccount {
        ACTIVE("ACTIVE"), INACTIVE("INACTIVE"), DORMANT("DORMANT");

        private String statusAccount;

        StatusAccount(String statusAccount) {
            this.statusAccount = statusAccount;
        }

    }

    public class idStatusAccount {

        public static final int ACTIVE = 1;
        public static final int INACTIVE = 2;
        public static final int DORMANT = 3;
    }

    public enum StockType {
        MEMBER("Member"),
        AGENT("Agent"),
        TRUE("True"),
        DEALER("Dealer"),
        SUPPLIER("Supplier");

        private String stockType;

        StockType(String stockType) {
            this.stockType = stockType;
        }

        public String getStockType() {
            return stockType;
        }
    }

    public enum TypeTrx {
        WITHDRAW("WITHDRAW"), DEPOSIT("DEPOSIT");

        private String typeTrx;

        TypeTrx(String typeTrx) {
            this.typeTrx = typeTrx;
        }

    }

    public enum StatusDana {
        DEDUCT("DEDUCT"), REFUND("REFUND");

        private String statusDana;

        StatusDana(String statusDana) {
            this.statusDana = statusDana;
        }

    }

    public class rc {

        public static final String exception = "01";
        public static final String SUCCESS = "0000";
        public static final String FAILED = "0001";
    }

    public enum Pesan {

        getFailedInquiry,
        getSuccessInquiry,
        getDataNotComplete,
        getFailedPartialRefund,
        getFailedRefund,
        getTimeoutRefund,
        getTimeoutPayment,
        getInvalidSign,
        UNKNOWN;

        public JsonObject pesan() {
            JsonObject checkResponse = new JsonObject();
            switch (this) {
                case getSuccessInquiry:
                    checkResponse.addProperty("message", "Success");
                    checkResponse.addProperty("rc", "00");
                    return checkResponse;

                case getFailedInquiry:
                    checkResponse.addProperty("message", "Gagal inquiry");
                    checkResponse.addProperty("rc", "004");
                    return checkResponse;
                case getDataNotComplete:
                    checkResponse.addProperty("message", "Required data for transaction is not complete");
//                    checkResponse.addProperty("message", "Parameter tidak komplit, Silakan Coba Beberapa Saat Lagi ");
                    checkResponse.addProperty("rc", "50");
                    return checkResponse;
                case getFailedPartialRefund:
                    checkResponse.addProperty("message", "Issuer does not support for partial refund");
                    checkResponse.addProperty("rc", "33");
                    return checkResponse;
                case getFailedRefund:
                    checkResponse.addProperty("message", "Transaction could not be refunded");
                    checkResponse.addProperty("rc", "32");
                    return checkResponse;
                case getTimeoutPayment:
                    checkResponse.addProperty("message", "Failed to payment transaction, please try again later");
                    checkResponse.addProperty("rc", "02");
                    return checkResponse;
               case getTimeoutRefund:
                    checkResponse.addProperty("message", "Failed to refund transaction, please try again later");
                    checkResponse.addProperty("rc", "02");
                    return checkResponse;
                case getInvalidSign:
                    checkResponse.addProperty("message", "Invalid Signature");
//                    checkResponse.addProperty("message", "Signature Tidak Valid");
                    checkResponse.addProperty("rc", "100");
                    return checkResponse;
                default:
                    throw new AssertionError("Unknown operations " + this);
            }

        }
    }

    private static String GenerateAngka() {
        final String alphabet = "0123456789";
        final int N = alphabet.length();
        Random r = new Random();
        StringBuilder Angka = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            Angka.append(alphabet.charAt(r.nextInt(N)));
        }
        return Angka.toString();
    }

    public static String generateIdTrx(String type) {
        String id_trx;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

        id_trx = "4" + type + sdf.format(new Date()) + GenerateAngka();
//        System.out.println("generateIdTrx --> " + id_trx);
        return id_trx;
    }

    public static Timestamp getServerTimestamp() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        return new Timestamp(date.getTime());
    }

}
