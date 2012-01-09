// perftimer.cpp : Defines the entry point for the DLL application.
//

#include "stdafx.h"
#include "perftimer.h"
#include "to_mumble_util_PrecisionTimer.h"

static double	multfactor = 0.0;
static bool		gIsOkay = false;

BOOL APIENTRY DllMain( HANDLE hModule, 
                       DWORD  ul_reason_for_call, 
                       LPVOID lpReserved
					 )
{
    switch (ul_reason_for_call)
	{
		case DLL_PROCESS_ATTACH:
		{
			//-- Get the performance timer's frequency,
			LARGE_INTEGER	lv;
			if(QueryPerformanceFrequency(&lv))
			{
				//-- Get the multiplication factor.
				multfactor	= 1000000.0 / lv.QuadPart;
				gIsOkay	= true;
			}
			else
				gIsOkay = false;
			return TRUE;
		}

		case DLL_THREAD_ATTACH:
		case DLL_THREAD_DETACH:
		case DLL_PROCESS_DETACH:
			break;
    }
    return TRUE;
}

JNIEXPORT jlong JNICALL Java_to_mumble_util_PrecisionTimer_getOsTimer(JNIEnv *, jclass)
{
	if(! gIsOkay) return 0;

	LARGE_INTEGER	lv;
	if(QueryPerformanceCounter(&lv))
	{
		return (__int64) (lv.QuadPart * multfactor);
	}
	return 0;
	

}


