/* Test the `vreinterpretu16_p8' ARM Neon intrinsic.  */
/* This file was autogenerated by neon-testgen.  */

/* { dg-do assemble } */
/* { dg-require-effective-target arm_neon_ok } */
/* { dg-options "-save-temps -O0" } */
/* { dg-add-options arm_neon } */

#include "arm_neon.h"

void test_vreinterpretu16_p8 (void)
{
  uint16x4_t out_uint16x4_t;
  poly8x8_t arg0_poly8x8_t;

  out_uint16x4_t = vreinterpret_u16_p8 (arg0_poly8x8_t);
}

/* { dg-final { cleanup-saved-temps } } */
