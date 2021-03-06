package ba.unsa.etf.rma.rma20siljakamina96.detail;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Map;

import ba.unsa.etf.rma.rma20siljakamina96.util.TransactionDBOpenHelper;

import static ba.unsa.etf.rma.rma20siljakamina96.detail.TransactionDetailPresenter.transactionDetailResultReceiver;
import static ba.unsa.etf.rma.rma20siljakamina96.list.TransactionListInteractor.transactionTypes;
import static ba.unsa.etf.rma.rma20siljakamina96.util.TransactionDBOpenHelper.TRANSACTION_ID;

public class TransactionListChange extends AsyncTask<String, Integer, Void> implements ITransactionListChange{
    final public static int STATUS_RUNNING=0;
    final public static int STATUS_FINISHED=1;
    final public static int STATUS_ERROR=2;
    private int id;
    private TransactionDBOpenHelper transactionDBOpenHelper;
    SQLiteDatabase database;
    private OnTransactionModifyDone caller;
    private String key = "a8dfa9fe-fe66-4026-9fb0-1c6abcdd0f10";

    public TransactionListChange(OnTransactionModifyDone p) {
        caller = p;
    }

    public TransactionListChange() {}

    @Override
    protected Void doInBackground(String... strings) {
        String body = getParametersInJSON(strings);
        try {
            URL url = null;
            String url1 = "http://rma20-app-rmaws.apps.us-west-1.starter.openshift-online.com/account/"+key + "/transactions";
            url1+= "/"+strings[7];
            id = Integer.valueOf(strings[7]);
            try {
                url = new URL(url1);
            }catch (MalformedURLException e){
                e.printStackTrace();
            }
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setDoInput(true);

            try(OutputStream os = urlConnection.getOutputStream()) {
                byte[] input = body.getBytes("utf-8");
                os.write(input,0,input.length);
            }
            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
        } catch (ClassCastException e) {
            transactionDetailResultReceiver.send(STATUS_ERROR, null);
        } catch (MalformedURLException e) {
            transactionDetailResultReceiver.send(STATUS_ERROR, null);
        } catch (IOException e) {
            transactionDetailResultReceiver.send(STATUS_ERROR, null);
        }
        return null;
    }

    private String getParametersInJSON(String... strings) {
        SimpleDateFormat DATE_FORMAT_SET= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        SimpleDateFormat DATE_FORMAT_GET= new SimpleDateFormat("dd-MM-yyyy");
        String body = "{";
//        "{"name": "Upendra", "job": "Programmer"}"
//        ???date???, ???title???, ???amount???, ???endDate???, ???itemDescription???, ???transactionInterval???, ???typeId???
        body += "\"date\": \""+strings[0]+"\", ";
        body += "\"title\": \""+strings[1]+"\", ";
        body += "\"amount\": "+strings[2];
        if(!(strings[3]).equals("")) {
            body += ", \"endDate\": \""+strings[3]+"\"";
        }
        if(!(strings[4]).equals("")) {
            body += ", \"itemDescription\": \""+strings[4]+"\"";
        }
        if(!(strings[5]).equals("")) {
            body += ", \"transactionInterval\": \""+strings[5]+"\"";
        }

        String typeId = "";
        for (Map.Entry<Integer,String> entry : transactionTypes.entrySet()) {
            if((strings[6]).equals( entry.getValue())) {
                typeId = entry.getKey().toString();
                break;
            }
        }
        body += ", \"TransactionTypeId\": "+typeId+"}";
        return body;
    }

    public interface OnTransactionModifyDone {
        public void onTransactionModified(int id);
    }
    @Override
    protected void onPostExecute(Void aVoid){
        super.onPostExecute(aVoid);
        caller.onTransactionModified(id);
    }


    @Override
    public void update(String date, Double amount, String title, String type, String itemDescription, Integer transactionInterval, String endDate, Integer id, Context context) {
        ContentResolver cr = context.getApplicationContext().getContentResolver();
        Uri transactionsURI = Uri.parse("content://rma.provider.transactions/elements");
        transactionDBOpenHelper = new TransactionDBOpenHelper(context);
//        database = transactionDBOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TRANSACTION_ID, id);
        values.put(transactionDBOpenHelper.TRANSACTION_TITLE,title);
        values.put(transactionDBOpenHelper.TRANSACTION_DATE, date);
        values.put(transactionDBOpenHelper.TRANSACTION_INTERVAL,transactionInterval);
        values.put(transactionDBOpenHelper.TRANSACTION_AMOUNT, amount);
        values.put(transactionDBOpenHelper.TRANSACTION_DESCRIPTION,itemDescription);
        values.put(transactionDBOpenHelper.TRANSACTION_TYPE, type);
        values.put(transactionDBOpenHelper.TRANSACTION_ENDDATE, endDate);
        values.put(transactionDBOpenHelper.TRANSACTION_CHANGE, "modify");
        cr.insert(transactionsURI, values);
    }
}
