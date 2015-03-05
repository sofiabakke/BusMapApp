package no.application.sofia.busmapapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import no.application.sofia.busmapapp.R;
import no.application.sofia.busmapapp.activities.MainActivity;


public class MapFragment extends Fragment {
    private static GoogleMap busMap;
    private LatLng myLocation;
    private static final String ARG_SECTION_NUMBER = "section_number";
    private boolean fromNavDrawer = false; //To check where the map was selected
    private static EditText searchField;


    public static MapFragment newInstance(int sectionNumber) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }



    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        searchField = (EditText) view.findViewById(R.id.search_box);
        searchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    searchForRoute(searchField.getText().toString());
                }
                // It must return false to remove the keyboard on submit
                return false;
            }
        });



        return view;
    }

    private void searchForRoute(String route){
        busMap.clear();

        final int lineID = Integer.parseInt(route);

        new Thread(new Runnable() {
            public void run() {
                addRouteLineToMap("Ruter", lineID);
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                addRouteMarkersToMap("Ruter", lineID);
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                addStopMarkersToMap("Ruter", lineID);
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER)); //Setting the Action Bar text
//        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded(){
        // Do a null check to confirm that we have not already instantiated the map.
        if (busMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            busMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.busmap)).getMap();
            // Check if we were successful in obtaining the map.
            if (busMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap(){
        busMap.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (fromNavDrawer) {
            if (lastKnownLocation != null) {
                //Try to use the last known location to set lat long
                myLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            } else {
                //If the app is not able to find the last known location, the map is centered to Oslo
                myLocation = new LatLng(59.9138688, 10.7522454);
            }
            fromNavDrawer = false;
            busMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 10));
        }
        else{
            //Setting the camera to pin on the location of a stop clicked from the stops fragment
            busMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 13));
            busMap.addMarker(new MarkerOptions().position(myLocation).title("Chosen Sop Location"));
        }
        busMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 10));

	    //addPolyline("clzlJelpaAdBsGp@_C`@e@XGb@Fp@lANxACjBJhALNl@@HAJjBn@xD~BtGZ~ATtDJhAEXBXJHf@zEfAzIbAzFlCdKd@xEJdCCRyB@a@CW^@n@Bj@lAlEjBdHpCnNj@vA~ErZfNxy@z@pDtB|GrB`FrB`ExBnEbChFjCnGv@dC|BvI|AjIhApIhB|RhBfPbEfYjAhMh@vJT~HAv]BtIMvBFvEDrCP~Dr@~HhAbLXxICnDkAna@MtQNjP`@|KN~IKrB]zAqA`BiAbAq@|@iAnBw@vB{@nBcA`CyAlDiAjCw@lA_Ar@i@PgBRwAFwCEkDEaCAsC^o@TwAx@w@p@{AfBgAfBo@zAuAvE_ArEoGhd@yBjNiBxHoCvJgCbHiBfD}BrCuAlAwBrAsJlDeGnCyBfAgEdByAl@qFzAmFdAeHrCiEnBoBvBi@z@_AhCmA~FqDdS}A`Io@lD]`DU`GC~CLtJFfLApHDbFIfBDbAHXNDLU@_@M[GsIDiAFQ@u@S]WLCH}A`@WA_AMqAy@cB{C{ByEcAeA_A]sDUmDOqCcAsA_@SoDeAcDgBkEeHgJgAaAoAm@eBcAaAqAeAqAuBmB}A{AqDaDiEyDYWb@kCc@jCXVvAlApBjBpD`D|AzAtBlBdApAbBlB~BhAz@l@jAtA~C~DnBtCxAtDt@tBR|ADjA@d@NDVF|Ah@fCr@tDHlBN~@Tn@^|@lApCbGl@dAf@d@`Af@z@H`@?fARBJBHJHL?Pa@JKl@KpBIFI?QAy@Ky@Si@uAaAi@a@Sc@Io@M}DEwEE}F@wD^yFb@aDzAoHfC_N~BsMpAyD~@}AvAqAhDgBfCeAjCgA|D_AvDu@~By@rFsBxDiBbEkBrGaClCeArD{BdCiCdBeC~@eBl@_BbE{MhEiQr@aEdCkQdEgYVoApAaEbA}BhAiB`C_CvBgAhCu@`@AtOCdBYrA}@lAiBpA}CvE{Kp@o@jAw@d@B^RxArBv@x@XBZKj@{AJoAOsF{@aL[cF[gKSeMBsOHsF^kMj@}P\\{MEkISwDi@cFqAuL[aFKiADqHCaa@]iKa@gH_AkKgByMiCcQ}AsO{AcPgBmLqBeJuBoHqBiF_BiD{CkGoFkLqCoIcAiEiBqJ_Nkz@kA{HQoAaAuHeCaUOiCDs@BSCk@GMEC_@aE{@mF}AmFk@mCsAqJ{@sHCyAO_@]qD{@gEy@_Co@aCYmBSkCU_IFwGRmIJkBVApAGh@_@b@sADqB[sJ");

		addPolyline("ojylJygsaAg@XUFQg@COOFm@^m@v@oA|Cy@jCgArE_@jBS|AWnCCf@ClA@`D@PqyylJmaraAVpHDdBCv@Kp@Qf@QXYP]D_@@w@DOAC`CApBBTCv@IlEChD?r@C|DBlAC?i@DMIMUGk@ByACmASeA]o@WOe@?c@Z[r@w@bDy@~CSj@clzlJelpaAwAhDsAtCa@r@c@\\_@Ty@\\aDrAyAh@k~zlJazoaA}Ar@iFpBiAn@u@x@g@bAmAtCgAdC_@h@eAv@wu{lJocoaAgAj@_@XQVqA|BY^a@`@_@VO?SFMLLjARvAJ~AZtD|@lKF`AF~@?xBQFMJc@n@Yr@i@xCET_@vAMFE^?RDRLDFAHSBSCS@a@N}Af@uCLa@`@u@^a@`@KCsAm{{lJwymaAC[ES]eEcAgLUiDC_AIqA\\g@^WZYLOhAoBj@}@^Yv@a@l@]f@c@^i@fAeClAuCf@cAt@y@RMDRHh@L|ACl@Kp@Sj@eA~AQ^If@@`BDrAE`AW`AOGYKi@a@KEM?_@Vi@l@MFeARQTE^EdABbBRdCPdA`@fAdAfBj@pAZvAPfCN|D?tBKn@u@lAm@oA[]{AkAc@WWEi@Au@BQFMJc@n@Yr@i@xCET_@vAMFE^m@|@W\\s@VA?we|lJcfmaAaDNwABeAJJtAHb@ao|lJiamaARt@Zh@z@j@~@\\p@Rv@`@kf|lJizlaAnAdAtA|Ap@~@~@jBXr@h@lAIXE@g@?CBk}{lJwjlaAPE^?HYn@vAx@nBlBtFnApEt@xC^lBqo{lJ_okaA_@mBu@yCoAqEmBuF}BoFc@_AKYSl@Ox@KnAApALjA^pBt@pD^dB~@lEThAtA~F\\hCHfCD|@d@j@^d@ar{lJ_{jaA^d@Zp@Rh@l@pCh@lDtAfIn@lCt@pB\\p@pB`DNV`@mBf@{BaA}Ac@cAk@kCo@uDqAqHu@gE}i{lJqyjaAfCxNn@tDj@jCb@bA`A|Ag@zBa@lBOWqBaD]q@u@qBi@}B}i{lJebjaAh@|Bt@pB\\p@pB`DNVCHab{lJ_siaA}@|DMz@O|AElBRfI?hAGt@A\\ICgAGm@AWJm@GwAOqAUs@Y_Am@aAaAoBeD_A_B{@kAe@i@SOSc@WSWMe@Mc@O{ASg@MCECEECG@KJGZ@PBPFHF@JEJS@INC`@Bf@H`A^lAd@RNL?RNd@h@z@jAnBdD~@~A`A`A~@l@r@XpATvANl@FTRbAAwf{lJcuhaAjAJ?WEAGAICgAGm@AWJm@GwAOqAUs@Y_Am@aAaA_A_BoBeD{@kAe@i@SOSc@WSWMiA]{ASg@MCEIIODGNAd@BPFHJ?NKBK@INC`@Bf@H`A^TF{a|lJeiiaAfAj@L?HF`@^\\d@~AbCXf@`AbBz@lAd@`@t@d@b@Tn@PlBXrALTRh@CtAHNBv@V^Hj@^t@p@n@r@fA`BbBvDnAvCz@lBr@nAh@l@b@RLLTHn@T^h@@B?FBF@@AnCKzA?v@Bd@H~@P|@FP_@hBoAbEiAdEaB~Fm@hCUzASjCQ|EQrD[lB_@fBSj@w@fBk@v@u@rAcAxA[COOWs@m@oEe@qDg@aDy@kDyAmFqD_N[kAe@_Bo@_BiAmBqA{Aq@}@aAaBc@_Ac@sAe@aBi@qCa@iCQcBSeDKaCYuIUaHUyDQsAQ_Aug|lJmciaAi@qBi@eAWa@q@u@]WyBaAmAg@sAo@g@]k@g@q@s@]e@_HiK_A}AU}@Ko@Ao@N{@NsADo@?u@Gy@UaA[g@OKSO_@I_@A_@Da@JGFm@t@i@z@Ql@Ez@Dp@\\bA`@bA\\jAHVDKVo@LKLBPJRV~AvBXTx@pAh@|@fEnGtAnBj@p@`Av@FDrAp@fDtA|@f@b@Z^d@z@`B\\nAT~@Hj@RfBXpGXfLVjG\\bEh@hDdAlEt@vBv@`BXd@vCrDz@tA`@|@fAjDlE`P~AdGh@dCf@bDtBfPfDrWDnAb@bFR|CLhI@ZJQj@O~Ag@RQN[NWH[Hg@Dg@?{@Bc@e@gEIFGP]V[Tg@\\SBGAEGIQ_@{CUwAOs@Sk@aDqVoBkOk@eEo@eDeA}DwByHeCkJg@eBo@_BYi@o@cA}@cAa@e@q@aAy@yAaAgCOi@i@{BWuAa@sCYeDM_CKaC?Ood|lJcghaAc@oMDmBGaDMwA_@{CQyAAq@BK?e@GOIIODGNAd@BPFHBt@?pC?d@FbB|@fKJt@TbIVjG\\bEh@hDdAlEt@vBv@`BXd@`ApAwy{lJibgaAnBdCd@v@jA`DvBzHnElPh@dCf@bDbA`Iwc{lJsheaAxD~Y^xCDnAp@bIJ~CBjBBzBSBe@JB^J~C{{zlJmicaAK_Dv@SL|HIdG@dBLbBRfAn@fC`A~DFj@Pj@R`CRhEBxCEzCGjBSrBa@nCaArDy@bDc@~B]fDOxBGdDBhDPhDNzAfCfSf@`Ef@nDF~Ap@|LDtB?ZGbAA@CFARBPFHF@FGJ[Zc@ZYd@UB?HEBK?QCOGIG?GKIUWkAgAyIKk@Qi@i@gE}AwLc@eDm@{E[yCK_BKgEB_DJcCRcC^wC`AcEz@uCZ}AZaCLuADeAFgB@eDGyCOcD]aEk@gFq@iHGwAE_BGmDFy@?aB_xzlJojcaAGwGSgFB[Vs@bAs@\\WJDb@zD@D@h@Al@Ih@Mn@OXYb@e@R{Ab@SFMIJpEBzDKdFDdBN|ARjAbArD^bBPhAPj@J|@NjCL`E?BqszlJ{zaaA?dDG`CO~BS`BUvAc@dBcArDm@tCSpAK`ASxCItB?dDJnDFjAXjCJbAmzzlJsy_aAlCxSr@hFF~AP|C^~GDtBG|ACDCP@PDNFBFCDM@CFMZc@ZYPMRGD?BADK@OAOEKGCIFCP@P_ozlJih~`ADLFBHEBM?QCMGIG@GJABy@dAi@^AAE?C@GJAPBPDJH@FLDPyqzlJ_c~`ALl@Ff@tBpU@FJ\\D^zAjOtA`NjGpn@j@~EhA~H\\nBJn@@b@@JVvAd@dCf@dBxAvEJZZxA?H?HFx@HVLPPLF@NCLKJQHWFo@Ey@Qk@KMMEKAE?_CsGs@cCk@aCYqACEIK]oByAqJYqB}AiOuHwu@yAiOG_AGy@Dq@?IOmFQ_H?m@H_B?C@E@OAQGIGAIHAFy@dAi@^CAE?GHAFKPk@~@uAvAkA`AMNQFO@QGUMu@i@a~zlJg_~`A}B_Ba@WIKCGKl@?VDj@@x@PjETvCZxDNhBSTa@Vub{lJqh}`A{CbB{Az@o@r@m@`BGFMIEMgn{lJk_}`ADLLHFGVs@Tm@n@s@`DgBvBoA|@uA\\w@\\iAZaBTeBHyAPgCFBNAPGLOjAaAtAwAb@SXU@@BBB?@APj@Jj@Fp@rBbUL`@x@jIrEhd@`Efa@nAxJj@nDVvA@h@j@`Dl@bC|@tCj@dBZlAHh@?HMh@Qf@g@`AQJCBY\\UZ[^mAtASLWXYJSZa@XcB`AgBv@_A^UDUM}@cB}CaG{@gBKOo@wAaAmBkAqBs@aAWe@Wi@aAwCa@aBaDcRq@iDe@gAUYu@u@yD_DSOUCMBWVSp@GZm@jCg@`ASRQFQ?e@]aByB{@oA[o@c@u@}@}@y@y@_BqByAcBYi@Ss@n@uBp@qAh@m@TMTIbASdASa~{lJkx{`AiCf@UHULi@l@q@pAADuf|lJer{`Am@nB{@jCiAjD_AjCkAzCO@_CHeu|lJi{z`Ac@?w@Fk@b@uA|AaAhA[j@g@`B_ApDe@nAo@jAk@r@kf}lJedz`AeFlEkAr@eAXaAX[PaAl@_BdA_BfAUP@pAF|C?HOH_~}lJkky`A_@Rq@b@a@^WVEDOEi@b@q@d@{@j@e@P_ACo@[}@y@GEAL]vA_@jAup~lJaay`Ai@fB]p@SN]BmBWa@@o@Vg@^QPaAfAESq@yDEKKEOLE`@Dl@Jb@y`_mJuzx`AFXNDXBL@DZBPBHOT]f@_@n@a@bAOr@Cd@An@FdALnA\\bD?b@Cl@Ed@c@|AU`AWbByc_mJexw`AmB`Nig_mJyhw`Ac@jCXVvAlA`FpEtGdGl@p@V^FHur~lJsov`Ah@t@p@l@~BhAz@l@jAtA~C~D`AlAl@fAxAtDt@tBR|ADjA@d@NDVFmx}lJaku`A|Ah@jAb@z@NDnB@`A?b@}p}lJcau`ACnDIbEQjGSrDWrCa@nCw@`ECNgv}lJyws`Aa@dD]`Egx}lJqls`AWtCc@bDk@dC_AzCy@zCm@lDk@fEc@rEe@fG[bFie~lJcsq`Ac@xJSxGMxFK`HAzAFnCLhBTrB\\vBRv@Dl@Cj@GNAL@THNLADK@Ied~lJcvo`ACQIII?GNAL@THNLAVn@Zd@Tf@n@~@vAxAfBbBb@\\\\Pn@Rh@Fj@?dBKeq}lJago`A~C@bAFfAR|@\\|@l@t@x@r@lAtBtEpAnD`CrGh@rA_u|lJahn`AXd@x@v@VLl@Pn@?f@KhAg@REXFZHRgBVkAXq@b@s@oe|lJenn`Ac@r@Yp@WjASfBHPt@bBr@zAZj@Zb@|FpGr@j@t@`ATh@fAbDZlAJV?HDNHFH@HIBE?C`@PXVr@`@PHh@BHAPCz@SFDLALMFY?]GYEIIEM@MI][Ym@y@cCeA}Dk@oBeDkR}BsL]iBa|{lJ_|n`AgBuJ}@aGy@qGMqCU_FQqGCaC@SD]A]IYMKMCIBGHENA^B\\FRArBHrEX|D`AnI`B|KlCjO~B~LvCxP?d@TxAFbA@|@Cd@Er@WdCMj@EBINCb@BPDLHFHAHI@G`@PXVr@`@PHh@BZEz@SBBF@LCJQD[A]IWGGECG@C@MI][Ym@y@cCeA}Dk@oBeDkR}BsLGsA@]Li@f@s@d@i@v@y@J?LGl@g@mt{lJyco`An@a@FAZAJJF?LIDOn@ULMRE|@EXB~Bb@nA^lBv@nAt@jAx@n@j@?L}zzlJs}n`A?MVRvAlA|BrBfBxA\\Pl@Nh@HjA?lAIXNvAGd@Ix@OF`@DHIPGNEJoazlJgqn`AVm@KWASJDJHH?dFo@xDe@csylJgvn`A^MLEvAcAx@cAfA{ArAmBghylJmao`A|@wArAyBtBcDlDsF\\]PCPB`Bx@t@Xj@Xl@TOZcBlDi@fAq@pAYp@EVQhA[~BcC`HKZYH[GMM{`ylJ{tn`AoAwE}AqGKo@UZiAlBoApB{@nAs@r@_@Xq@XQFmATsE^oC\\YF?J@b@Xz@~AfEzAhErEfO~ApEnAxCJZb@aARq@VkAXcAv@eBdB}Cn@gAR]Pm@l@yCHILyATuBa`ylJkin`AVmCBmAeAyDuAqFkAcFCKCQGHIJCD@NH`@T|@`AxDjAlERt@r@jBf@tA|AbE|@rCr@rAr@zAzDzJFPcpxlJspm`AlBvFn@xB`DzKlFbRf@pBcAlBq@lAcaxlJs{k`AmDdGeI`OgDjGIBa@j@WRq@XcCTEFU@_@CeCEYD]Fg@^]p@Of@WxBWvBO`@a@t@_AtAcBhCsCdE_ApAkAfAcAp@a@Nu@VcANyABo@CeAMoBi@gAk@iAu@]WoAmAgAwA{A}B_CiDe@_Aw@kCw@}Ce@kBC[W_Am@aAc@i@SQCOIc@AUDsALaCl@{KPoD@aEMsEQeCc@eDi@mCImAg@wCQ_BMyAEwADeA@K@_@GYCIIGM?MNGX?NGVIHYLSDS?UCc@Sk@]_@WUSAKCSGKICIDINCNBd@DLHFHAHI@G`@PXVr@`@PHL@");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //When the some other fragment in the navigation drawer is selected, the busMap is set to
        // null again to be able to setup the map when the fragment is reattached.
        busMap = null;
        Log.d("onDetach", "In MapFragment");
    }

    public void setFromNavDrawer(boolean fromNavDrawer){
        this.fromNavDrawer = fromNavDrawer;
    }

    //This needs to be called before replacing a stops fragment to this one in order to zoom to the stops location
    public void setMyLocation(double lat, double lng){
        myLocation = new LatLng(lat, lng);
    }
    private void addRouteLineToMap(String operator, int lineID){
	    final JSONObject busLine = getBusLineInfo(operator, lineID);



        // Forces main thread to add polyline to the map, as this can not be done in a separate thread
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
	            try {
		            addPolyline(busLine.getString("Polyline"));
	            }catch(Exception e){
		            e.printStackTrace();
	            }
            }
        });


    }

	private JSONObject getBusLineInfo(String operator, int lineID){
		String URL = "http://api.bausk.no/Stops/getBusLineInfo/" + operator + "/" + lineID;

		String busStopJSONs = sendJSONRequest(URL);
		JSONObject json = new JSONObject();
		try {
			json = new JSONObject(busStopJSONs);
			Log.i(MapFragment.class.getName(), json.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return json;
	}

    private void addRouteMarkersToMap(String operator, int lineID){
        JSONArray buses = getBusPositionsOnLine(operator, lineID);
        for(int i = 0; i < buses.length(); i++){
            try {
                final JSONObject json = buses.getJSONObject(i);
                final JSONObject positionJSON = json.getJSONObject("Position");
                final LatLng pos = new LatLng(positionJSON.getDouble("Latitude"), positionJSON.getDouble("Longitude"));

                // Forces main thread to add markers to the map, as this can not be done in a separate thread
                final Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
//                            Log.d("Transportation", json.getInt("Transportation") + "");
                            int transportation = json.getInt("Transportation");
                            if (transportation == 7)
                                busMap.addMarker(new MarkerOptions()
                                        .title("Line: " + json.getString("LineID") + " VehicleID: " + json.getString("VehicleID"))
                                        .position(pos)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_tram))
                                        .snippet("HELLO THERE!"));
                            else if (transportation == 8)
                                busMap.addMarker(new MarkerOptions()
                                        .title("Line: " + json.getString("LineID") + " VehicleID: " + json.getString("VehicleID"))
                                        .position(pos)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_sub))
                                        .snippet("HELLO THERE!"));
                            else
                                busMap.addMarker(new MarkerOptions()
                                        .title("Line: " + json.getString("LineID") + " VehicleID: " + json.getString("VehicleID"))
                                        .position(pos)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_bus))
                                        .snippet("HELLO THERE!"));
                            busMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 10));
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                });

            }catch(JSONException e){
                e.printStackTrace();
            }
        }
    }

    private void addStopMarkersToMap(String operator, int lineID){
        Log.d("Stops", "Adding Stops");
        JSONArray stops = getBusStopsOnLine(operator, lineID);
        for (int i = 0; i < stops.length(); i++){
            try{
                Log.d("try", "First Try");
                final JSONObject json = stops.getJSONObject(i);
                final JSONObject positionJSON = json.getJSONObject("Position");
                final LatLng pos = new LatLng(positionJSON.getDouble("Latitude"), positionJSON.getDouble("Longitude"));

                final Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            busMap.addMarker(new MarkerOptions()
                            .title("Name: " + json.getString("Name"))
                            .position(pos)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_stop))
                            .snippet("STOOOOP"));
                            busMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 10));
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private JSONArray getBusStopsOnLine(String operator, int lineID){
        String URL = "http://api.bausk.no/Stops/getBusStopsOnLine/" + operator + "/" + lineID;

        String busStopJSONs = sendJSONRequest(URL);
        JSONArray json = new JSONArray();
        try {
            json = new JSONArray(busStopJSONs);
            Log.i(MapFragment.class.getName(), json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return json;

    }

    private JSONArray getBusPositionsOnLine(String operator, int lineID){
        String URL = "http://api.bausk.no/Bus/getBusPositionsOnLine/" + operator + "/" + lineID;

        String busJSONs = sendJSONRequest(URL);
        JSONArray json = new JSONArray();
        try {
            json = new JSONArray(busJSONs);
            Log.i(MapFragment.class.getName(), json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return json;
    }

    // Downloads JSON from a given URL
    private String sendJSONRequest(String URL){
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(URL);
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } else {
                Log.e(MapFragment.class.getName(), "Failed to download file");
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

	private void addPolyline(String encodedLine){
		List<LatLng> waypoints = PolyUtil.decode(encodedLine);
		final PolylineOptions rectOptions = new PolylineOptions()
			.addAll(waypoints);

		busMap.addPolyline(rectOptions);
	}
}
