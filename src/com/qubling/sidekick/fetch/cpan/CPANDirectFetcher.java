package com.qubling.sidekick.fetch.cpan;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import com.qubling.sidekick.fetch.SerialUpdateFetcher;
import com.qubling.sidekick.fetch.UpdateFetcher;
import com.qubling.sidekick.model.Instance;
import com.qubling.sidekick.model.Model;
import com.qubling.sidekick.search.ResultsForUpdate;

import android.util.Log;

public abstract class CPANDirectFetcher<SomeInstance extends Instance<SomeInstance>> 
	extends CPANFetcher<SomeInstance> 
	implements UpdateFetcher<SomeInstance> {
	
	/**
	 * An enumeration of direct retrieval URLs on MetaCPAN.
	 *
	 * @author sterling
	 *
	 */
    public enum FetchSection {
    	MODULE_POD(METACPAN_API_POD_URL),
    	MODULE_FETCH(METACPAN_API_MODULE_URL);

        private String baseUrl;

        FetchSection(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getBaseUrl() {
            return baseUrl;
        }
    }
    
    private FetchSection fetchSection;
    
    public CPANDirectFetcher(Model<SomeInstance> model, FetchSection fetchSection) {
    	super(model);
    	
    	this.fetchSection  = fetchSection;
    }

	@Override
	protected void execute() {
		Log.d("CPANDirectFetcher", "START execute()");
		
		for (SomeInstance instance : getResultSet()) {
			fetchOne(instance);
		}
		
		Log.d("CPANDirectFetcher", "END execute()");
	}
	
    protected void fetchOne(SomeInstance instance) {
        String fetchContent;
        try {
            HttpGet fetchRequest = new HttpGet(fetchSection.getBaseUrl() + instance.getKey());
            HttpResponse fetchResponse = getHttpClient().execute(fetchRequest);

            fetchContent = slurpContent(fetchResponse);
            
            consumeResponse(fetchContent, instance);
        }

        catch (IOException e) {
            Log.e("CPANDirectFetcher", "Cannot fetch from " + fetchSection.getBaseUrl() + instance.getKey() + ": " + e.getMessage(), e);
        }
    }
    
    public abstract void consumeResponse(String content, SomeInstance instance);

	@Override
	public void setIncomingResultSet(ResultsForUpdate<SomeInstance> input) {
		setResultSet(input);
	}

	public FetchSection getFetchSection() {
    	return fetchSection;
    }
	
	@Override
	public String toString() {
		return getModel() + ":CPANDirectFetcher(" + fetchSection + ";" + getResultSet() + ")";
	}

	@Override
	public SerialUpdateFetcher<SomeInstance> thenDoFetch(UpdateFetcher<SomeInstance> fetcher) {
		return super.thenDoFetch(fetcher);
	}

}
