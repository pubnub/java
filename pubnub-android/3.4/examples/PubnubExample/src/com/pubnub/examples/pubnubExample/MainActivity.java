package com.pubnub.examples.pubnubExample;

import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;

public class MainActivity extends Activity {
	/*
	 * //Noise Test Pubnub pubnub = new
	 * Pubnub("pub-c-87f96934-8c44-4f8d-a35f-deaa2753f083",
	 * "sub-c-3a693cf8-7401-11e2-8b02-12313f022c90", "", false); String channel
	 * = "noise"; String channel = "a";
	 */

	Pubnub pubnub = new Pubnub("demo", "demo", "", false);

	private void notifyUser(Object message) {
		try {
			if (message instanceof JSONObject) {
				final JSONObject obj = (JSONObject) message;
				this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(getApplicationContext(), obj.toString(),
								Toast.LENGTH_LONG).show();

						Log.i("Received msg : ", String.valueOf(obj));
					}
				});

			} else if (message instanceof String) {
				final String obj = (String) message;
				this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(getApplicationContext(), obj,
								Toast.LENGTH_LONG).show();
						Log.i("Received msg : ", obj.toString());
					}
				});

			} else if (message instanceof JSONArray) {
				final JSONArray obj = (JSONArray) message;
				this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(getApplicationContext(), obj.toString(),
								Toast.LENGTH_LONG).show();
						Log.i("Received msg : ", obj.toString());
					}
				});
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.usage);
		this.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent intent) {
				pubnub.disconnectAndResubscribe();

			}

		}, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {

		case R.id.option1:
			subscribe();
			return true;

		case R.id.option2:
			publish();
			return true;

		case R.id.option3:
			presence();
			return true;

		case R.id.option4:
			detailedHistory();
			return true;

		case R.id.option5:
			hereNow();
			return true;

		case R.id.option6:
			unsubscribe();
			return true;

		case R.id.option7:
			presenceUnsubscribe();
			return true;

		case R.id.option8:
			time();
			return true;

		case R.id.option9:
			disconnectAndResubscribe();
			return true;

		case R.id.option10:
			disconnectAndResubscribeWithTimetoken();
			return true;

		case R.id.option11:
			toggleResumeOnReconnect();
			return true;

		case R.id.option12:
			setMaxRetries();
			return true;

		case R.id.option13:
			setRetryInterval();
			return true;

		case R.id.option14:
			setSubscribeTimeout();
			return true;

		case R.id.option15:
			setNonSubscribeTimeout();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void setNonSubscribeTimeout() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Set Non Subscribe Timeout");
		builder.setMessage("Enter timeout value in milliseconds");
		final EditText edTimeout = new EditText(this);
		edTimeout.setInputType(InputType.TYPE_CLASS_NUMBER);
		builder.setView(edTimeout);
		builder.setPositiveButton("Done",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						pubnub.setNonSubscribeTimeout(Integer
								.parseInt(edTimeout.getText().toString()));
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void subscribe() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Subscribe");
		builder.setMessage("Enter channel name");
		final EditText input = new EditText(this);
		builder.setView(input);
		builder.setPositiveButton("Subscribe",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						Hashtable args = new Hashtable(1);

						String message = input.getText().toString();
						args.put("channel", message);

						try {
							pubnub.subscribe(args, new Callback() {
								public void connectCallback(String channel,
										Object message) {
									notifyUser("SUBSCRIBE : CONNECT on channel:"
											+ channel
											+ " : "
											+ message.getClass()
											+ " : "
											+ message.toString());
								}

								public void disconnectCallback(String channel,
										Object message) {
									notifyUser("SUBSCRIBE : DISCONNECT on channel:"
											+ channel
											+ " : "
											+ message.getClass()
											+ " : "
											+ message.toString());
								}

								@Override
								public void reconnectCallback(String channel,
										Object message) {
									notifyUser("SUBSCRIBE : RECONNECT on channel:"
											+ channel
											+ " : "
											+ message.getClass()
											+ " : "
											+ message.toString());
								}

								public void successCallback(String channel,
										Object message) {
									notifyUser("SUBSCRIBE : " + channel + " : "
											+ message.getClass() + " : "
											+ message.toString());
								}

								public void errorCallback(String channel,
										Object message) {
									notifyUser("SUBSCRIBE : ERROR on channel "
											+ channel + " : "
											+ message.getClass() + " : "
											+ message.toString());
								}
							});

						} catch (Exception e) {

						}
					}

				});
		AlertDialog alert = builder.create();
		alert.show();

	}

	private void _publish(final String channel) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Publish");
		builder.setMessage("Enter message");
		final EditText etMessage = new EditText(this);
		builder.setView(etMessage);
		builder.setPositiveButton("Publish",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Hashtable args = new Hashtable(2);

						String message = etMessage.getText().toString();

						if (args.get("message") == null) {
							try {
								Integer i = Integer.parseInt(message);
								args.put("message", i);
							} catch (Exception e) {

							}
						}
						if (args.get("message") == null) {
							try {
								Double d = Double.parseDouble(message);
								args.put("message", d);
							} catch (Exception e) {

							}
						}
						if (args.get("message") == null) {
							try {
								JSONArray js = new JSONArray(message);
								args.put("message", js);
							} catch (Exception e) {

							}
						}
						if (args.get("message") == null) {
							try {
								JSONObject js = new JSONObject(message);
								args.put("message", js);
							} catch (Exception e) {

							}
						}
						if (args.get("message") == null) {
							args.put("message", message);
						}

						// Publish Message

						args.put("channel", channel); // Channel Name

						pubnub.publish(args, new Callback() {
							public void successCallback(String channel,
									Object message) {
								notifyUser("PUBLISH : " + message);
							}

							public void errorCallback(String channel,
									Object message) {
								notifyUser("PUBLISH : " + message);
							}
						});
					}

				});
		AlertDialog alert = builder.create();
		alert.show();

	}

	private void publish() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Publish ");
		builder.setMessage("Enter channel name");
		final EditText etChannel = new EditText(this);
		builder.setView(etChannel);
		builder.setPositiveButton("Done",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						_publish(etChannel.getText().toString());
					}

				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void presence() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Presence");
		builder.setMessage("Enter channel name");
		final EditText input = new EditText(this);
		builder.setView(input);
		builder.setPositiveButton("Subscribe For Presence",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String channel = input.getText().toString();

						try {
							pubnub.presence(channel, new Callback() {

								public void successCallback(String channel,
										Object message) {
									notifyUser("PRESENCE : " + channel + " : "
											+ message.getClass() + " : "
											+ message.toString());
								}

								public void errorCallback(String channel,
										Object message) {
									notifyUser("PRESENCE : ERROR on channel "
											+ channel + " : "
											+ message.getClass() + " : "
											+ message.toString());
								}
							});

						} catch (Exception e) {

						}
					}

				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void detailedHistory() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Detailed History");
		builder.setMessage("Enter channel name");
		final EditText input = new EditText(this);
		builder.setView(input);
		builder.setPositiveButton("Get detailed history",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String channel = input.getText().toString();
						pubnub.detailedHistory(channel, 2, new Callback() {
							public void successCallback(String channel,
									Object message) {
								notifyUser("DETAILED HISTORY : " + message);
							}

							public void errorCallback(String channel,
									Object message) {
								notifyUser("DETAILED HISTORY : " + message);
							}
						});
					}

				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void hereNow() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Here Now");
		builder.setMessage("Enter channel name");
		final EditText input = new EditText(this);
		builder.setView(input);
		builder.setPositiveButton("Get Here Now",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String channel = input.getText().toString();
						pubnub.hereNow(channel, new Callback() {
							public void successCallback(String channel,
									Object message) {
								notifyUser("HERE NOW : " + message);
							}

							public void errorCallback(String channel,
									Object message) {
								notifyUser("HERE NOW : " + message);
							}
						});
					}

				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void unsubscribe() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Unsubscribe");
		builder.setMessage("Enter channel name");
		final EditText input = new EditText(this);
		builder.setView(input);
		builder.setPositiveButton("Unsubscribe",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String channel = input.getText().toString();
						pubnub.unsubscribe(channel);
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void presenceUnsubscribe() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Unsubscribe Presence");
		builder.setMessage("Enter channel name");
		final EditText input = new EditText(this);
		builder.setView(input);
		builder.setPositiveButton("Unsubscribe Presence",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String channel = input.getText().toString();
						pubnub.unsubscribePresence(channel);
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void time() {
		pubnub.time(new Callback() {
			public void successCallback(String channel, Object message) {
				notifyUser("TIME : " + message);
			}

			public void errorCallback(String channel, Object message) {
				notifyUser("TIME : " + message);
			}
		});

	}

	private void disconnectAndResubscribe() {
		pubnub.disconnectAndResubscribe("Disconnect and Resubscribe Sent from Demo Console");

	}

	private void setSubscribeTimeout() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Set Subscribe Timeout");
		builder.setMessage("Enter timeout value in milliseconds");
		final EditText edTimeout = new EditText(this);
		edTimeout.setInputType(InputType.TYPE_CLASS_NUMBER);
		builder.setView(edTimeout);
		builder.setPositiveButton("Done",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						pubnub.setSubscribeTimeout(Integer.parseInt(edTimeout
								.getText().toString()));
					}

				});
		AlertDialog alert = builder.create();
		alert.show();

	}

	private void setRetryInterval() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Set Retry Interval");
		builder.setMessage("Enter retry interval in milliseconds");
		final EditText edInterval = new EditText(this);
		edInterval.setInputType(InputType.TYPE_CLASS_NUMBER);
		builder.setView(edInterval);
		builder.setPositiveButton("Done",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						pubnub.setRetryInterval(Integer.parseInt(edInterval
								.getText().toString()));
					}

				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void toggleResumeOnReconnect() {
		pubnub.setResumeOnReconnect(pubnub.isResumeOnReconnect() ? false : true);
		notifyUser("RESUME ON RECONNECT : " + pubnub.isResumeOnReconnect());
	}

	private void setMaxRetries() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Set Max Retries");
		builder.setMessage("Enter Max Retries");
		final EditText edRetries = new EditText(this);
		edRetries.setInputType(InputType.TYPE_CLASS_NUMBER);
		builder.setView(edRetries);
		builder.setPositiveButton("Done",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						pubnub.setMaxRetries(Integer.parseInt(edRetries
								.getText().toString()));
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void disconnectAndResubscribeWithTimetoken() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Disconnect and Resubscribe with timetoken");
		builder.setMessage("Enter Timetoken");
		final EditText edTimetoken = new EditText(this);
		builder.setView(edTimetoken);
		builder.setPositiveButton("Done",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						pubnub.disconnectAndResubscribeWithTimetoken(
								edTimetoken.getText().toString(),
								"Disconnect and Resubscribe Sent from Demo Console");
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}
}
