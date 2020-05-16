package ba.unsa.etf.rma.rma20siljakamina96.list;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ba.unsa.etf.rma.rma20siljakamina96.data.FinanceModel;
import ba.unsa.etf.rma.rma20siljakamina96.data.Transaction;
import ba.unsa.etf.rma.rma20siljakamina96.data.Type;

public class TransactionListInteractor extends AsyncTask<String, Integer, Void> implements ITransactionInteractor {

    private String tmdb_api_key = "";
    private OnTransactionGetDone caller;
    ArrayList<Transaction> transactions;

    @Override
    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public TransactionListInteractor(OnTransactionGetDone p) {
        caller = p;
        transactions = new ArrayList<Transaction>();
    };
//    @Override
//    public void delete(Transaction transaction) {
//        Iterator itr = FinanceModel.transactions.iterator();
//        while (itr.hasNext())
//        {
//            Transaction t = (Transaction)itr.next();
//            if (t.equals(transaction))
//                itr.remove();
//        }
//    }
//    @Override
//    public void save(Transaction transaction, String title, double amount, Type type, String itemDescription, Date date) {
//        for(Transaction t: FinanceModel.transactions) {
//            if (t.getTitle().equals(transaction.getTitle())) {
//                t.setTitle(title);
//                t.setAmount(amount);
//                t.setItemDescription(itemDescription);
//                t.setType(type);
//                t.setDate(date);
//            }
//        }
//    }
//    @Override
//    public void save(Transaction transaction, String title, double amount, Type type, String itemDescription, int transactionInterval, Date date, Date endDate) {
//        for(Transaction t: FinanceModel.transactions) {
//            if (t.getTitle().equals(transaction.getTitle())) {
//                t.setTitle(title);
//                t.setAmount(amount);
//                t.setItemDescription(itemDescription);
//                t.setTransactionInterval(transactionInterval);
//                t.setType(type);
//                t.setDate(date);
//                t.setEndDate(endDate);
//            }
//        }
//    }
//    @Override
//    public void add(String title, double amount, Type type, String itemDescription, int transactionInterval, Date date, Date endDate) {
//        FinanceModel.transactions.add(new Transaction(date, amount, title, type, itemDescription, transactionInterval, endDate));
//    }
//    @Override
//    public void add(String title, double amount, Type type, String itemDescription, Date date) {
//        FinanceModel.transactions.add(new Transaction(date, amount, title, type, itemDescription));
//    }

    public String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new
                InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
        return sb.toString();
    }
    @Override
    protected Void doInBackground(String... strings) {

        String url1 = "http://rma20-app-rmaws.apps.us-west-1.starter.openshift-online.com/account/a8dfa9fe-fe66-4026-9fb0-1c6abcdd0f10/transactions";
        try {
            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            URL url = new URL(url1);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            String result = convertStreamToString(in);
            JSONObject jo = new JSONObject(result);
            JSONArray results = jo.getJSONArray("transactions");
            for (int i = 0; i < results.length(); i++) {

                JSONObject transaction = results.getJSONObject(i);

                Integer id = transaction.getInt("id");
                Date date = DATE_FORMAT.parse(transaction.getString("date"));
                String title = transaction.getString("title");
                Double amount = transaction.getDouble("amount");
                String itemDescription = transaction.getString("itemDescription");

                Integer transactionInterval = 0;
                if (!transaction.isNull("transactionInterval")) {
                    transactionInterval = Integer.valueOf(transaction.getString("transactionInterval"));
                }

                Date endDate = null;
                if (!transaction.isNull("endDate")) {
                    endDate = DATE_FORMAT.parse(transaction.getString("endDate"));
                }

                transactions.add(new Transaction(date,amount,title,Type.valueOf("REGULARPAYMENT"),itemDescription,transactionInterval,endDate));
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    public interface OnTransactionGetDone{
        public void onTransactionGetDone(ArrayList<Transaction> results);
    }
    @Override
    protected void onPostExecute(Void aVoid){
        super.onPostExecute(aVoid);
        caller.onTransactionGetDone(transactions);
    }
}