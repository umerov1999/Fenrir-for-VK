cmake_minimum_required(VERSION 4.1.2 FATAL_ERROR)
project(fenrir_jni C CXX ASM)

if (${CMAKE_BUILD_TYPE} STREQUAL "Debug")
    set(CMAKE_CXX_FLAGS "-std=c++23 -DANDROID -g -DDEBUG_PLATFORM")
    set(CMAKE_C_FLAGS "-std=c23 -w -DANDROID -D_LARGEFILE_SOURCE -g -DDEBUG_PLATFORM")
    set(OPTIMIZE_NORMAL "-O0")
    set(OPTIMIZE_FAST "-O0")
else ()
    set(CMAKE_CXX_FLAGS "-std=c++23 -DANDROID -DFENRIR_NATIVE_LOG_DISABLED -DNDEBUG -ffunction-sections -fdata-sections -fmerge-all-constants")
    set(CMAKE_C_FLAGS "-std=c23 -w -DANDROID -DFENRIR_NATIVE_LOG_DISABLED -DNDEBUG -D_LARGEFILE_SOURCE -ffunction-sections -fdata-sections -fmerge-all-constants")
    set(OPTIMIZE_NORMAL "-Os")
    set(OPTIMIZE_FAST "-O3")
    set(CMAKE_CXX_VISIBILITY_PRESET hidden)
    set(CMAKE_VISIBILITY_INLINES_HIDDEN ON)
endif ()
set(CMAKE_ASM_FLAGS "${CFLAGS} -x assembler-with-cpp")
set(CMAKE_CXX_STANDARD 23)
set(CMAKE_C_STANDARD 23)

set(ffmpeg_location "$ENV{HOME}/ffmpeg")
set(ffmpeg_binaries "${ffmpeg_location}/android-libs/${ANDROID_ABI}")

foreach (ffmpeg_lib avutil swresample avcodec avformat swscale)
    set(ffmpeg_lib_filename lib${ffmpeg_lib}.a)
    set(ffmpeg_lib_file_path ${ffmpeg_binaries}/${ffmpeg_lib_filename})
    add_library(
            ${ffmpeg_lib}
            STATIC
            IMPORTED)
    set_target_properties(
            ${ffmpeg_lib} PROPERTIES
            IMPORTED_LOCATION
            ${ffmpeg_lib_file_path})
endforeach ()

add_library(thorvg STATIC
        animation/thorvg/src/common/tvgCompressor.cpp
        animation/thorvg/src/common/tvgMath.cpp
        animation/thorvg/src/common/tvgStr.cpp
        animation/thorvg/src/loaders/lottie/tvgLottieAnimation.cpp
        animation/thorvg/src/loaders/lottie/tvgLottieBuilder.cpp
        animation/thorvg/src/loaders/lottie/tvgLottieExpressions.cpp
        animation/thorvg/src/loaders/lottie/tvgLottieInterpolator.cpp
        animation/thorvg/src/loaders/lottie/tvgLottieLoader.cpp
        animation/thorvg/src/loaders/lottie/tvgLottieModel.cpp
        animation/thorvg/src/loaders/lottie/tvgLottieModifier.cpp
        animation/thorvg/src/loaders/lottie/tvgLottieParser.cpp
        animation/thorvg/src/loaders/lottie/tvgLottieParserHandler.cpp
        animation/thorvg/src/loaders/lottie/tvgLottieProperty.cpp
        animation/thorvg/src/loaders/raw/tvgRawLoader.cpp
        animation/thorvg/src/loaders/svg/tvgSvgBuilder.cpp
        animation/thorvg/src/loaders/svg/tvgSvgCssStyle.cpp
        animation/thorvg/src/loaders/svg/tvgSvgLoader.cpp
        animation/thorvg/src/loaders/svg/tvgSvgPath.cpp
        animation/thorvg/src/loaders/svg/tvgSvgUtil.cpp
        animation/thorvg/src/loaders/svg/tvgXmlParser.cpp
        animation/thorvg/src/renderer/cpu_engine/tvgSwBlendOp.cpp
        animation/thorvg/src/renderer/cpu_engine/tvgSwFill.cpp
        animation/thorvg/src/renderer/cpu_engine/tvgSwImage.cpp
        animation/thorvg/src/renderer/cpu_engine/tvgSwMemPool.cpp
        animation/thorvg/src/renderer/cpu_engine/tvgSwPostEffect.cpp
        animation/thorvg/src/renderer/cpu_engine/tvgSwRaster.cpp
        animation/thorvg/src/renderer/cpu_engine/tvgSwRasterCore.cpp
        animation/thorvg/src/renderer/cpu_engine/tvgSwRasterSimd.cpp
        animation/thorvg/src/renderer/cpu_engine/tvgSwRenderer.cpp
        animation/thorvg/src/renderer/cpu_engine/tvgSwRle.cpp
        animation/thorvg/src/renderer/cpu_engine/tvgSwShape.cpp
        animation/thorvg/src/renderer/cpu_engine/tvgSwStroke.cpp
        animation/thorvg/src/renderer/cpu_engine/tvgSwUtil.cpp
        animation/thorvg/src/renderer/tvgAccessor.cpp
        animation/thorvg/src/renderer/tvgAnimation.cpp
        animation/thorvg/src/renderer/tvgCanvas.cpp
        animation/thorvg/src/renderer/tvgFill.cpp
        animation/thorvg/src/renderer/tvgInitializer.cpp
        animation/thorvg/src/renderer/tvgLoaderMgr.cpp
        animation/thorvg/src/renderer/tvgPaint.cpp
        animation/thorvg/src/renderer/tvgPicture.cpp
        animation/thorvg/src/renderer/tvgRender.cpp
        animation/thorvg/src/renderer/tvgSaver.cpp
        animation/thorvg/src/renderer/tvgScene.cpp
        animation/thorvg/src/renderer/tvgShape.cpp
        animation/thorvg/src/renderer/tvgTaskScheduler.cpp
        animation/thorvg/src/renderer/tvgText.cpp)

target_compile_options(thorvg PRIVATE
        -DTVG_STATIC -DBIG_ENDIAN_COLORS -fno-exceptions ${OPTIMIZE_NORMAL} -fno-rtti -fno-unwind-tables -fno-asynchronous-unwind-tables -Woverloaded-virtual -Wno-unused-parameter -Wno-nan-infinity-disabled)
target_include_directories(thorvg PRIVATE
        animation/thorvg/inc
        animation/thorvg/src/common
        animation/thorvg/src/loaders/lottie
        animation/thorvg/src/loaders/raw
        animation/thorvg/src/loaders/svg
        animation/thorvg/src/renderer
        animation/thorvg/src/renderer/cpu_engine)

if (${ANDROID_ABI} STREQUAL "arm64-v8a" OR ${ANDROID_ABI} STREQUAL "armeabi-v7a")
    target_compile_options(thorvg PRIVATE -DRAPIDJSON_NEON)
else ()
    target_compile_options(thorvg PRIVATE -mavx2 -DRAPIDJSON_SSE42)
endif ()

#####################################################################
add_library(libyuv STATIC
        animation/libyuv/source/compare.cc
        animation/libyuv/source/compare_common.cc
        animation/libyuv/source/compare_gcc.cc
        animation/libyuv/source/compare_hvx.cc
        animation/libyuv/source/compare_la.cc
        animation/libyuv/source/compare_neon64.cc
        animation/libyuv/source/compare_neon.cc
        animation/libyuv/source/convert_argb.cc
        animation/libyuv/source/convert.cc
        animation/libyuv/source/convert_from_argb.cc
        animation/libyuv/source/convert_from.cc
        animation/libyuv/source/convert_jpeg.cc
        animation/libyuv/source/convert_to_argb.cc
        animation/libyuv/source/convert_to_i420.cc
        animation/libyuv/source/cpu_id.cc
        animation/libyuv/source/mjpeg_decoder.cc
        animation/libyuv/source/mjpeg_validate.cc
        animation/libyuv/source/planar_functions.cc
        animation/libyuv/source/rotate_any.cc
        animation/libyuv/source/rotate_argb.cc
        animation/libyuv/source/rotate.cc
        animation/libyuv/source/rotate_common.cc
        animation/libyuv/source/rotate_gcc.cc
        animation/libyuv/source/rotate_hvx.cc
        animation/libyuv/source/rotate_la.cc
        animation/libyuv/source/rotate_neon64.cc
        animation/libyuv/source/rotate_neon.cc
        animation/libyuv/source/rotate_rvv.cc
        animation/libyuv/source/rotate_sme.cc
        animation/libyuv/source/rotate_sve.cc
        animation/libyuv/source/row_any.cc
        animation/libyuv/source/row_common.cc
        animation/libyuv/source/row_gcc.cc
        animation/libyuv/source/row_hvx.cc
        animation/libyuv/source/row_la.cc
        animation/libyuv/source/row_neon64.cc
        animation/libyuv/source/row_neon.cc
        animation/libyuv/source/row_rvv.cc
        animation/libyuv/source/row_sme.cc
        animation/libyuv/source/row_sve.cc
        animation/libyuv/source/scale_any.cc
        animation/libyuv/source/scale_argb.cc
        animation/libyuv/source/scale.cc
        animation/libyuv/source/scale_common.cc
        animation/libyuv/source/scale_gcc.cc
        animation/libyuv/source/scale_hvx.cc
        animation/libyuv/source/scale_la.cc
        animation/libyuv/source/scale_neon64.cc
        animation/libyuv/source/scale_neon.cc
        animation/libyuv/source/scale_rgb.cc
        animation/libyuv/source/scale_rvv.cc
        animation/libyuv/source/scale_sme.cc
        animation/libyuv/source/scale_uv.cc
        animation/libyuv/source/video_common.cc)
target_compile_options(libyuv PRIVATE ${OPTIMIZE_NORMAL} -funroll-loops -fno-strict-aliasing -fno-math-errno)
target_include_directories(libyuv PRIVATE animation/libyuv/include)
if (${ANDROID_ABI} STREQUAL "arm64-v8a")
    target_compile_options(libyuv PRIVATE -march=armv8-a+dotprod+i8mm -DLIBYUV_DISABLE_SVE -DLIBYUV_DISABLE_SME)
endif ()

#####################################################################
add_library(libopus STATIC
        audio/opus/libopus/src/analysis.c
        audio/opus/libopus/src/extensions.c
        audio/opus/libopus/src/mapping_matrix.c
        audio/opus/libopus/src/mlp.c
        audio/opus/libopus/src/mlp_data.c
        audio/opus/libopus/src/opus.c
        audio/opus/libopus/src/opus_compare.c
        audio/opus/libopus/src/opus_decoder.c
        audio/opus/libopus/src/opus_demo.c
        audio/opus/libopus/src/opus_encoder.c
        audio/opus/libopus/src/opus_multistream.c
        audio/opus/libopus/src/opus_multistream_decoder.c
        audio/opus/libopus/src/opus_multistream_encoder.c
        audio/opus/libopus/src/opus_projection_decoder.c
        audio/opus/libopus/src/opus_projection_encoder.c
        audio/opus/libopus/src/qext_compare.c
        audio/opus/libopus/src/repacketizer.c
        audio/opus/libopus/silk/A2NLSF.c
        audio/opus/libopus/silk/ana_filt_bank_1.c
        audio/opus/libopus/silk/biquad_alt.c
        audio/opus/libopus/silk/bwexpander_32.c
        audio/opus/libopus/silk/bwexpander.c
        audio/opus/libopus/silk/check_control_input.c
        audio/opus/libopus/silk/CNG.c
        audio/opus/libopus/silk/code_signs.c
        audio/opus/libopus/silk/control_audio_bandwidth.c
        audio/opus/libopus/silk/control_codec.c
        audio/opus/libopus/silk/control_SNR.c
        audio/opus/libopus/silk/debug.c
        audio/opus/libopus/silk/dec_API.c
        audio/opus/libopus/silk/decode_core.c
        audio/opus/libopus/silk/decode_frame.c
        audio/opus/libopus/silk/decode_indices.c
        audio/opus/libopus/silk/decode_parameters.c
        audio/opus/libopus/silk/decode_pitch.c
        audio/opus/libopus/silk/decode_pulses.c
        audio/opus/libopus/silk/decoder_set_fs.c
        audio/opus/libopus/silk/enc_API.c
        audio/opus/libopus/silk/encode_indices.c
        audio/opus/libopus/silk/encode_pulses.c
        audio/opus/libopus/silk/gain_quant.c
        audio/opus/libopus/silk/HP_variable_cutoff.c
        audio/opus/libopus/silk/init_decoder.c
        audio/opus/libopus/silk/init_encoder.c
        audio/opus/libopus/silk/inner_prod_aligned.c
        audio/opus/libopus/silk/interpolate.c
        audio/opus/libopus/silk/lin2log.c
        audio/opus/libopus/silk/log2lin.c
        audio/opus/libopus/silk/LPC_analysis_filter.c
        audio/opus/libopus/silk/LPC_fit.c
        audio/opus/libopus/silk/LPC_inv_pred_gain.c
        audio/opus/libopus/silk/LP_variable_cutoff.c
        audio/opus/libopus/silk/NLSF2A.c
        audio/opus/libopus/silk/NLSF_decode.c
        audio/opus/libopus/silk/NLSF_del_dec_quant.c
        audio/opus/libopus/silk/NLSF_encode.c
        audio/opus/libopus/silk/NLSF_stabilize.c
        audio/opus/libopus/silk/NLSF_unpack.c
        audio/opus/libopus/silk/NLSF_VQ.c
        audio/opus/libopus/silk/NLSF_VQ_weights_laroia.c
        audio/opus/libopus/silk/NSQ.c
        audio/opus/libopus/silk/NSQ_del_dec.c
        audio/opus/libopus/silk/pitch_est_tables.c
        audio/opus/libopus/silk/PLC.c
        audio/opus/libopus/silk/process_NLSFs.c
        audio/opus/libopus/silk/quant_LTP_gains.c
        audio/opus/libopus/silk/resampler.c
        audio/opus/libopus/silk/resampler_down2_3.c
        audio/opus/libopus/silk/resampler_down2.c
        audio/opus/libopus/silk/resampler_private_AR2.c
        audio/opus/libopus/silk/resampler_private_down_FIR.c
        audio/opus/libopus/silk/resampler_private_IIR_FIR.c
        audio/opus/libopus/silk/resampler_private_up2_HQ.c
        audio/opus/libopus/silk/resampler_rom.c
        audio/opus/libopus/silk/shell_coder.c
        audio/opus/libopus/silk/sigm_Q15.c
        audio/opus/libopus/silk/sort.c
        audio/opus/libopus/silk/stereo_decode_pred.c
        audio/opus/libopus/silk/stereo_encode_pred.c
        audio/opus/libopus/silk/stereo_find_predictor.c
        audio/opus/libopus/silk/stereo_LR_to_MS.c
        audio/opus/libopus/silk/stereo_MS_to_LR.c
        audio/opus/libopus/silk/stereo_quant_pred.c
        audio/opus/libopus/silk/sum_sqr_shift.c
        audio/opus/libopus/silk/table_LSF_cos.c
        audio/opus/libopus/silk/tables_gain.c
        audio/opus/libopus/silk/tables_LTP.c
        audio/opus/libopus/silk/tables_NLSF_CB_NB_MB.c
        audio/opus/libopus/silk/tables_NLSF_CB_WB.c
        audio/opus/libopus/silk/tables_other.c
        audio/opus/libopus/silk/tables_pitch_lag.c
        audio/opus/libopus/silk/tables_pulses_per_block.c
        audio/opus/libopus/silk/VAD.c
        audio/opus/libopus/silk/VQ_WMat_EC.c
        audio/opus/libopus/silk/fixed/apply_sine_window_FIX.c
        audio/opus/libopus/silk/fixed/autocorr_FIX.c
        audio/opus/libopus/silk/fixed/burg_modified_FIX.c
        audio/opus/libopus/silk/fixed/corrMatrix_FIX.c
        audio/opus/libopus/silk/fixed/encode_frame_FIX.c
        audio/opus/libopus/silk/fixed/find_LPC_FIX.c
        audio/opus/libopus/silk/fixed/find_LTP_FIX.c
        audio/opus/libopus/silk/fixed/find_pitch_lags_FIX.c
        audio/opus/libopus/silk/fixed/find_pred_coefs_FIX.c
        audio/opus/libopus/silk/fixed/k2a_FIX.c
        audio/opus/libopus/silk/fixed/k2a_Q16_FIX.c
        audio/opus/libopus/silk/fixed/LTP_analysis_filter_FIX.c
        audio/opus/libopus/silk/fixed/LTP_scale_ctrl_FIX.c
        audio/opus/libopus/silk/fixed/noise_shape_analysis_FIX.c
        audio/opus/libopus/silk/fixed/pitch_analysis_core_FIX.c
        audio/opus/libopus/silk/fixed/process_gains_FIX.c
        audio/opus/libopus/silk/fixed/regularize_correlations_FIX.c
        audio/opus/libopus/silk/fixed/residual_energy16_FIX.c
        audio/opus/libopus/silk/fixed/residual_energy_FIX.c
        audio/opus/libopus/silk/fixed/schur64_FIX.c
        audio/opus/libopus/silk/fixed/schur_FIX.c
        audio/opus/libopus/silk/fixed/vector_ops_FIX.c
        audio/opus/libopus/silk/fixed/warped_autocorrelation_FIX.c
        audio/opus/libopus/silk/float/apply_sine_window_FLP.c
        audio/opus/libopus/silk/float/autocorrelation_FLP.c
        audio/opus/libopus/silk/float/burg_modified_FLP.c
        audio/opus/libopus/silk/float/bwexpander_FLP.c
        audio/opus/libopus/silk/float/corrMatrix_FLP.c
        audio/opus/libopus/silk/float/encode_frame_FLP.c
        audio/opus/libopus/silk/float/energy_FLP.c
        audio/opus/libopus/silk/float/find_LPC_FLP.c
        audio/opus/libopus/silk/float/find_LTP_FLP.c
        audio/opus/libopus/silk/float/find_pitch_lags_FLP.c
        audio/opus/libopus/silk/float/find_pred_coefs_FLP.c
        audio/opus/libopus/silk/float/inner_product_FLP.c
        audio/opus/libopus/silk/float/k2a_FLP.c
        audio/opus/libopus/silk/float/LPC_analysis_filter_FLP.c
        audio/opus/libopus/silk/float/LPC_inv_pred_gain_FLP.c
        audio/opus/libopus/silk/float/LTP_analysis_filter_FLP.c
        audio/opus/libopus/silk/float/LTP_scale_ctrl_FLP.c
        audio/opus/libopus/silk/float/noise_shape_analysis_FLP.c
        audio/opus/libopus/silk/float/pitch_analysis_core_FLP.c
        audio/opus/libopus/silk/float/process_gains_FLP.c
        audio/opus/libopus/silk/float/regularize_correlations_FLP.c
        audio/opus/libopus/silk/float/residual_energy_FLP.c
        audio/opus/libopus/silk/float/scale_copy_vector_FLP.c
        audio/opus/libopus/silk/float/scale_vector_FLP.c
        audio/opus/libopus/silk/float/schur_FLP.c
        audio/opus/libopus/silk/float/sort_FLP.c
        audio/opus/libopus/silk/float/warped_autocorrelation_FLP.c
        audio/opus/libopus/silk/float/wrappers_FLP.c
        audio/opus/libopus/celt/bands.c
        audio/opus/libopus/celt/celt.c
        audio/opus/libopus/celt/celt_decoder.c
        audio/opus/libopus/celt/celt_encoder.c
        audio/opus/libopus/celt/celt_lpc.c
        audio/opus/libopus/celt/cwrs.c
        audio/opus/libopus/celt/entcode.c
        audio/opus/libopus/celt/entdec.c
        audio/opus/libopus/celt/entenc.c
        audio/opus/libopus/celt/kiss_fft.c
        audio/opus/libopus/celt/laplace.c
        audio/opus/libopus/celt/mathops.c
        audio/opus/libopus/celt/mdct.c
        audio/opus/libopus/celt/mini_kfft.c
        audio/opus/libopus/celt/modes.c
        audio/opus/libopus/celt/pitch.c
        audio/opus/libopus/celt/quant_bands.c
        audio/opus/libopus/celt/rate.c
        audio/opus/libopus/celt/vq.c

        audio/opus/opusfile/src/info.c
        audio/opus/opusfile/src/internal.c
        audio/opus/opusfile/src/opusfile.c
        audio/opus/opusfile/src/stream.c
        audio/opus/ogg/src/bitwise.c
        audio/opus/ogg/src/framing.c
        audio/opus/rnnoise/src/celt_lpc.c
        audio/opus/rnnoise/src/denoise.c
        audio/opus/rnnoise/src/pitch.c
        audio/opus/rnnoise/src/rnn.c
        audio/opus/rnnoise/src/rnn_data.c
        audio/opus/rnnoise/src/rnn_reader.c
)
if (${ANDROID_ABI} STREQUAL "armeabi-v7a" OR ${ANDROID_ABI} STREQUAL "arm64-v8a")
    if (${ANDROID_ABI} STREQUAL "arm64-v8a")
        target_compile_options(libopus PRIVATE -march=armv8-a+dotprod+i8mm -DOPUS_ARM_PRESUME_AARCH64_NEON_INTR -DOPUS_ARM_PRESUME_DOTPROD)
    endif ()

    target_compile_options(libopus PRIVATE -DOPUS_ARM_MAY_HAVE_NEON -DOPUS_ARM_MAY_HAVE_NEON_INTR -DOPUS_ARM_PRESUME_NEON -DOPUS_ARM_PRESUME_NEON_INTR)
    target_sources(libopus PRIVATE
            audio/opus/libopus/celt/arm/arm_celt_map.c
            audio/opus/libopus/celt/arm/armcpu.c
            audio/opus/libopus/celt/arm/celt_neon_intr.c
            audio/opus/libopus/celt/arm/pitch_neon_intr.c
            audio/opus/libopus/silk/arm/arm_silk_map.c
            audio/opus/libopus/silk/arm/biquad_alt_neon_intr.c
            audio/opus/libopus/silk/arm/LPC_inv_pred_gain_neon_intr.c
            audio/opus/libopus/silk/arm/NSQ_del_dec_neon_intr.c
            audio/opus/libopus/silk/arm/NSQ_neon.c
            audio/opus/libopus/silk/fixed/arm/warped_autocorrelation_FIX_neon_intr.c)
else ()
    target_compile_options(libopus PRIVATE -mavx2 -mfma -DCPU_INFO_BY_C -DOPUS_X86_MAY_HAVE_AVX2 -DOPUS_X86_MAY_HAVE_SSE -DOPUS_X86_MAY_HAVE_SSE2 -DOPUS_X86_PRESUME_SSE -DOPUS_X86_PRESUME_SSE2 -DOPUS_X86_PRESUME_AVX2)
    target_sources(libopus PRIVATE
            audio/opus/libopus/celt/x86/pitch_avx.c
            audio/opus/libopus/celt/x86/pitch_sse2.c
            audio/opus/libopus/celt/x86/pitch_sse.c
            audio/opus/libopus/celt/x86/vq_sse2.c
            audio/opus/libopus/celt/x86/x86_celt_map.c
            audio/opus/libopus/celt/x86/x86cpu.c
            audio/opus/libopus/silk/x86/NSQ_del_dec_avx2.c
            audio/opus/libopus/silk/x86/x86_silk_map.c
            audio/opus/libopus/silk/float/x86/inner_product_FLP_avx2.c)
endif ()
target_include_directories(libopus PRIVATE
        audio/opus/libopus/include
        audio/opus/libopus/silk
        audio/opus/libopus/silk/fixed
        audio/opus/libopus/silk/float
        audio/opus/libopus/celt
        audio/opus/libopus
        audio/opus/ogg/include
        audio/opus/opusfile/include
        audio/opus/rnnoise/include)
target_compile_options(libopus PRIVATE
        ${OPTIMIZE_NORMAL} -funroll-loops -fno-strict-aliasing -fno-math-errno -DDISABLE_DEBUG_FLOAT -DENABLE_HARDENING -DHAVE_ALLOCA_H -DHAVE_LRINT -DHAVE_LRINTF -DOPUS_BUILD -DOPUS_HAVE_RTCD -DVAR_ARRAYS -DCUSTOM_MODES)

#####################################################################
add_library(common STATIC
        audio/ts/MpegTsDemuxer.cpp
        audio/taglib/mp4/mp4atom.cpp
        audio/taglib/mp4/mp4chapter.cpp
        audio/taglib/mp4/mp4coverart.cpp
        audio/taglib/mp4/mp4file.cpp
        audio/taglib/mp4/mp4item.cpp
        audio/taglib/mp4/mp4itemfactory.cpp
        audio/taglib/mp4/mp4nerochapterlist.cpp
        audio/taglib/mp4/mp4properties.cpp
        audio/taglib/mp4/mp4qtchapterlist.cpp
        audio/taglib/mp4/mp4stem.cpp
        audio/taglib/mp4/mp4tag.cpp
        audio/taglib/mpeg/id3v1/id3v1genres.cpp
        audio/taglib/mpeg/id3v1/id3v1tag.cpp
        audio/taglib/mpeg/id3v2/frames/attachedpictureframe.cpp
        audio/taglib/mpeg/id3v2/frames/chapterframe.cpp
        audio/taglib/mpeg/id3v2/frames/commentsframe.cpp
        audio/taglib/mpeg/id3v2/frames/eventtimingcodesframe.cpp
        audio/taglib/mpeg/id3v2/frames/generalencapsulatedobjectframe.cpp
        audio/taglib/mpeg/id3v2/frames/ownershipframe.cpp
        audio/taglib/mpeg/id3v2/frames/podcastframe.cpp
        audio/taglib/mpeg/id3v2/frames/popularimeterframe.cpp
        audio/taglib/mpeg/id3v2/frames/privateframe.cpp
        audio/taglib/mpeg/id3v2/frames/relativevolumeframe.cpp
        audio/taglib/mpeg/id3v2/frames/synchronizedlyricsframe.cpp
        audio/taglib/mpeg/id3v2/frames/tableofcontentsframe.cpp
        audio/taglib/mpeg/id3v2/frames/textidentificationframe.cpp
        audio/taglib/mpeg/id3v2/frames/uniquefileidentifierframe.cpp
        audio/taglib/mpeg/id3v2/frames/unknownframe.cpp
        audio/taglib/mpeg/id3v2/frames/unsynchronizedlyricsframe.cpp
        audio/taglib/mpeg/id3v2/frames/urllinkframe.cpp
        audio/taglib/mpeg/id3v2/id3v2extendedheader.cpp
        audio/taglib/mpeg/id3v2/id3v2footer.cpp
        audio/taglib/mpeg/id3v2/id3v2frame.cpp
        audio/taglib/mpeg/id3v2/id3v2framefactory.cpp
        audio/taglib/mpeg/id3v2/id3v2header.cpp
        audio/taglib/mpeg/id3v2/id3v2synchdata.cpp
        audio/taglib/mpeg/id3v2/id3v2tag.cpp
        audio/taglib/mpeg/mpegfile.cpp
        audio/taglib/mpeg/mpegheader.cpp
        audio/taglib/mpeg/mpegproperties.cpp
        audio/taglib/mpeg/xingheader.cpp
        audio/taglib/toolkit/tbytevector.cpp
        audio/taglib/toolkit/tbytevectorlist.cpp
        audio/taglib/toolkit/tbytevectorstream.cpp
        audio/taglib/toolkit/tcharset.cpp
        audio/taglib/toolkit/tdebug.cpp
        audio/taglib/toolkit/tdebuglistener.cpp
        audio/taglib/toolkit/tdeferredwritestream.cpp
        audio/taglib/toolkit/tfile.cpp
        audio/taglib/toolkit/tfilestream.cpp
        audio/taglib/toolkit/tiostream.cpp
        audio/taglib/toolkit/tpicturetype.cpp
        audio/taglib/toolkit/tpropertymap.cpp
        audio/taglib/toolkit/tstring.cpp
        audio/taglib/toolkit/tstringlist.cpp
        audio/taglib/toolkit/tvariant.cpp
        audio/taglib/toolkit/tversionnumber.cpp
        audio/taglib/toolkit/tzlib.cpp
        audio/taglib/audioproperties.cpp
        audio/taglib/fileref.cpp
        audio/taglib/tag.cpp
        audio/taglib/tagunion.cpp
        audio/taglib/tagutils.cpp
        compress/lz4/lz4.c
        compress/zstd/dictBuilder/fastcover.c
        compress/zstd/dictBuilder/cover.c
        compress/zstd/dictBuilder/zdict.c
        compress/zstd/dictBuilder/divsufsort.c
        compress/zstd/decompress/huf_decompress.c
        compress/zstd/decompress/zstd_decompress.c
        compress/zstd/decompress/zstd_ddict.c
        compress/zstd/decompress/zstd_decompress_block.c
        compress/zstd/common/xxhash.c
        compress/zstd/common/error_private.c
        compress/zstd/common/pool.c
        compress/zstd/common/fse_decompress.c
        compress/zstd/common/threading.c
        compress/zstd/common/debug.c
        compress/zstd/common/zstd_common.c
        compress/zstd/common/entropy_common.c
        compress/zstd/compress/zstdmt_compress.c
        compress/zstd/compress/zstd_fast.c
        compress/zstd/compress/zstd_compress_sequences.c
        compress/zstd/compress/zstd_lazy.c
        compress/zstd/compress/zstd_compress_literals.c
        compress/zstd/compress/hist.c
        compress/zstd/compress/zstd_double_fast.c
        compress/zstd/compress/huf_compress.c
        compress/zstd/compress/zstd_ldm.c
        compress/zstd/compress/zstd_opt.c
        compress/zstd/compress/zstd_preSplit.c
        compress/zstd/compress/zstd_compress_superblock.c
        compress/zstd/decompress/huf_decompress_amd64.S
        compress/zstd/compress/fse_compress.c
        compress/zstd/compress/zstd_compress.c
)

target_include_directories(common
        PRIVATE
        ./
        audio/taglib
        audio/taglib/mp4
        audio/taglib/mpeg
        audio/taglib/mpeg/id3v1
        audio/taglib/mpeg/id3v2
        audio/taglib/mpeg/id3v2/frames
        audio/taglib/toolkit
        audio/taglib/utf8
        audio/taglib/utf8/utf8
        compress/lz4
        compress/zstd
        compress/zstd/common
        compress/zstd/compress
        compress/zstd/decompress
        compress/zstd/dictBuilder)

target_compile_options(common PRIVATE
        ${OPTIMIZE_FAST} -DZSTD_MULTITHREAD -DZSTD_LEGACY_SUPPORT=0 -DTAGLIB_STATIC -funroll-loops -fno-strict-aliasing -fno-math-errno -Wno-unused-parameter -Wno-unused-variable)

#####################################################################
add_library(zxing STATIC
        zxing/aztec/AZDecoder.cpp
        zxing/aztec/AZDetector.cpp
        zxing/aztec/AZEncoder.cpp
        zxing/aztec/AZHighLevelEncoder.cpp
        zxing/aztec/AZReader.cpp
        zxing/aztec/AZToken.cpp
        zxing/aztec/AZWriter.cpp
        zxing/datamatrix/DMBitLayout.cpp
        zxing/datamatrix/DMDataBlock.cpp
        zxing/datamatrix/DMDecoder.cpp
        zxing/datamatrix/DMDetector.cpp
        zxing/datamatrix/DMECEncoder.cpp
        zxing/datamatrix/DMHighLevelEncoder.cpp
        zxing/datamatrix/DMReader.cpp
        zxing/datamatrix/DMSymbolInfo.cpp
        zxing/datamatrix/DMVersion.cpp
        zxing/datamatrix/DMWriter.cpp
        zxing/libzueci/zueci.c
        zxing/maxicode/MCBitMatrixParser.cpp
        zxing/maxicode/MCDecoder.cpp
        zxing/maxicode/MCReader.cpp
        zxing/oned/ODDataBarReader.cpp
        zxing/oned/ODCode39Reader.cpp
        zxing/oned/ODCodabarReader.cpp
        zxing/oned/ODUPCEANCommon.cpp
        zxing/oned/ODCodabarWriter.cpp
        zxing/oned/ODCode128Reader.cpp
        zxing/oned/ODITFReader.cpp
        zxing/oned/ODTelepenReader.cpp
        zxing/oned/ODDataBarCommon.cpp
        zxing/oned/ODDXFilmEdgeReader.cpp
        zxing/oned/ODDataBarLimitedReader.cpp
        zxing/oned/ODCode93Writer.cpp
        zxing/oned/ODCode39Writer.cpp
        zxing/oned/ODUPCAWriter.cpp
        zxing/oned/ODMultiUPCEANReader.cpp
        zxing/oned/ODEAN13Writer.cpp
        zxing/oned/ODWriterHelper.cpp
        zxing/oned/ODReader.cpp
        zxing/oned/ODUPCEWriter.cpp
        zxing/oned/ODCode93Reader.cpp
        zxing/oned/ODEAN8Writer.cpp
        zxing/oned/ODCode128Writer.cpp
        zxing/oned/ODDataBarExpandedReader.cpp
        zxing/oned/ODITFWriter.cpp
        zxing/oned/ODDataBarExpandedBitDecoder.cpp
        zxing/pdf417/PDFReader.cpp
        zxing/pdf417/PDFBarcodeValue.cpp
        zxing/pdf417/PDFScanningDecoder.cpp
        zxing/pdf417/ZXBigInteger.cpp
        zxing/pdf417/PDFCodewordDecoder.cpp
        zxing/pdf417/PDFEncoder.cpp
        zxing/pdf417/PDFDecoder.cpp
        zxing/pdf417/MicroPDFReader.cpp
        zxing/pdf417/PDF417.cpp
        zxing/pdf417/PDFHighLevelEncoder.cpp
        zxing/pdf417/PDFWriter.cpp
        zxing/pdf417/PDFDetectionResult.cpp
        zxing/pdf417/PDFDetector.cpp
        zxing/pdf417/PDFDetectionResultColumn.cpp
        zxing/pdf417/PDFBoundingBox.cpp
        zxing/qrcode/QRBitMatrixParser.cpp
        zxing/qrcode/QRVersion.cpp
        zxing/qrcode/QRMaskUtil.cpp
        zxing/qrcode/QRErrorCorrectionLevel.cpp
        zxing/qrcode/QRWriter.cpp
        zxing/qrcode/QRDecoder.cpp
        zxing/qrcode/QRFormatInformation.cpp
        zxing/qrcode/QREncoder.cpp
        zxing/qrcode/QRReader.cpp
        zxing/qrcode/QRDetector.cpp
        zxing/qrcode/QRMatrixUtil.cpp
        zxing/qrcode/QRDataBlock.cpp
        zxing/qrcode/QRCodecMode.cpp
        zxing/TextDecoder.cpp
        zxing/GridSampler.cpp
        zxing/GTIN.cpp
        zxing/WhiteRectDetector.cpp
        zxing/BarcodeFormat.cpp
        zxing/ZXingCpp.cpp
        zxing/BitArray.cpp
        zxing/WriteBarcode.cpp
        zxing/BitSource.cpp
        zxing/BitMatrix.cpp
        zxing/Barcode.cpp
        zxing/HybridBinarizer.cpp
        zxing/ReadBarcode.cpp
        zxing/TextEncoder.cpp
        zxing/BinaryBitmap.cpp
        zxing/ZXingC.cpp
        zxing/GlobalHistogramBinarizer.cpp
        zxing/Error.cpp
        zxing/BitMatrixIO.cpp
        zxing/Utf.cpp
        zxing/HRI.cpp
        zxing/ResultPoint.cpp
        zxing/Content.cpp
        zxing/MultiFormatWriter.cpp
        zxing/LocalGrid.cpp
        zxing/PerspectiveTransform.cpp
        zxing/ReedSolomon.cpp
        zxing/CharacterSet.cpp
        zxing/ConcentricFinder.cpp
        zxing/JSON.cpp
        zxing/CreateBarcode.cpp
        zxing/ECI.cpp
        zxing/MultiFormatReader.cpp
)

target_include_directories(zxing
        PRIVATE
        zxing
        zxing/aztec
        zxing/datamatrix
        zxing/libzueci
        zxing/maxicode
        zxing/oned
        zxing/pdf417
        zxing/qrcode
)

target_compile_options(zxing PRIVATE
        ${OPTIMIZE_FAST} -DZXING_INTERNAL -funroll-loops -fno-strict-aliasing -fno-math-errno -Wno-unused-parameter -Wno-unused-variable)

#####################################################################
add_library(fenrir_jni
        SHARED
        jni_call.cpp
        animation/thorvg/thorvg_jni.cpp
        animation/video/gifvideo_jni.cpp
        audio/ffmpeg_exoplayer_jni.cpp
        audio/mpegts_demux_jni.cpp
        audio/taglib_jni.cpp
        audio/opus/opus_jni.cc
        audio/audio_recorder_jni.cpp
        camerax/image_processing_util_jni.cpp
        camerax/surface_util_jni.cpp
        parcel_jni.cpp
        string_exist_jni.cpp
        file_utils_jni.cpp
        zstd_kmp_jni.cpp
        zxing_jni.cpp
)

target_include_directories(fenrir_jni
        PRIVATE
        ./
        animation/thorvg/inc
        animation/libyuv/include
        compress/lz4
        compress/zstd
        compress/zstd/common
        compress/zstd/compress
        compress/zstd/decompress
        compress/zstd/dictBuilder
        zxing
        zxing/qrcode
        audio/opus/libopus/include
        audio/opus/ogg/include
        audio/opus/opusfile/include
        audio/opus/rnnoise/include
        audio/taglib
        audio/taglib/mp4
        audio/taglib/mpeg
        audio/taglib/mpeg/id3v1
        audio/taglib/mpeg/id3v2
        audio/taglib/mpeg/id3v2/frames
        audio/taglib/toolkit
        ${ffmpeg_location})

target_compile_options(fenrir_jni PRIVATE
        ${OPTIMIZE_NORMAL} -funroll-loops -fno-strict-aliasing -fno-math-errno -Wno-unused-parameter -Wno-unused-variable)

if (${ANDROID_ABI} STREQUAL "arm64-v8a")
    target_compile_options(fenrir_jni PRIVATE -march=armv8-a+sha2)
elseif (${ANDROID_ABI} STREQUAL "x86_64")
    target_compile_options(fenrir_jni PRIVATE -msha)
endif ()

target_link_libraries(fenrir_jni
        PRIVATE android
        PRIVATE jnigraphics
        PRIVATE log
        PRIVATE swscale
        PRIVATE avformat
        PRIVATE avcodec
        PRIVATE swresample
        PRIVATE avutil
        PRIVATE common
        PRIVATE thorvg
        PRIVATE libyuv
        PRIVATE libopus
        PRIVATE zxing)

if (NOT ${CMAKE_BUILD_TYPE} STREQUAL "Debug")
    target_link_options(fenrir_jni PRIVATE "-Wl,--build-id=none" "-Wl,--exclude-libs,ALL")
endif ()